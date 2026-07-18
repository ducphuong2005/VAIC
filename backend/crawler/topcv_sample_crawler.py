#!/usr/bin/env python3
"""
Sample TopCV crawler for Career Compass.

It collects a small market sample and sends it to the internal backend API.
The parser is intentionally conservative because TopCV markup can change; when
no listing is detected, it falls back to a few deterministic sample postings so
the import/recalculate pipeline can still be tested locally.
"""

from __future__ import annotations

import hashlib
import html
import json
import os
import re
import sys
from datetime import datetime, timezone
from typing import Any
from urllib.error import URLError
from urllib.request import Request, urlopen


BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080")
API_KEY = os.getenv("INTERNAL_CRAWLER_API_KEY", "dev-crawler-api-key-change-me")
TOPCV_SEARCH_URL = os.getenv("TOPCV_SEARCH_URL", "https://www.topcv.vn/tim-viec-lam-data")


def http_json(method: str, path: str, payload: dict[str, Any] | None = None) -> dict[str, Any]:
    body = None if payload is None else json.dumps(payload).encode("utf-8")
    request = Request(
        f"{BACKEND_URL.rstrip('/')}{path}",
        data=body,
        method=method,
        headers={
            "Accept": "application/json",
            "Content-Type": "application/json",
            "X-Internal-Api-Key": API_KEY,
        },
    )
    with urlopen(request, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))


def fetch_topcv_html() -> str:
    request = Request(
        TOPCV_SEARCH_URL,
        headers={
            "User-Agent": "CareerCompassSampleCrawler/1.0 (+local development)",
            "Accept-Language": "vi,en;q=0.8",
        },
    )
    with urlopen(request, timeout=30) as response:
        return response.read().decode("utf-8", errors="ignore")


def parse_jobs(document: str) -> list[dict[str, Any]]:
    anchors = re.findall(r'<a[^>]+href="([^"]*topcv\.vn[^"]*)"[^>]*>(.*?)</a>', document, flags=re.I | re.S)
    jobs: list[dict[str, Any]] = []
    seen: set[str] = set()
    for url, raw_title in anchors:
        title = clean_html(raw_title)
        if not looks_like_job(title) or url in seen:
            continue
        seen.add(url)
        jobs.append(to_job(title, url, description="TopCV listing parsed from search result page."))
        if len(jobs) >= 8:
            break
    return jobs


def clean_html(value: str) -> str:
    value = re.sub(r"<[^>]+>", " ", value)
    value = html.unescape(value)
    return re.sub(r"\s+", " ", value).strip()


def looks_like_job(title: str) -> bool:
    if len(title) < 8 or len(title) > 160:
        return False
    lowered = title.lower()
    keywords = ("data", "dữ liệu", "developer", "software", "marketing", "analyst", "engineer", "lập trình")
    return any(keyword in lowered for keyword in keywords)


def to_job(title: str, url: str, description: str) -> dict[str, Any]:
    onet_code, skills = infer_onet_and_skills(title + " " + description)
    content_hash = hashlib.sha256(f"{title}|{url}|{description}".encode("utf-8")).hexdigest()
    return {
        "sourceName": "TopCV",
        "externalId": hashlib.sha1(url.encode("utf-8")).hexdigest()[:32],
        "contentHash": content_hash,
        "title": title,
        "companyName": None,
        "location": "Vietnam",
        "region": "Vietnam",
        "salaryMin": None,
        "salaryMax": None,
        "remote": "remote" in title.lower(),
        "entryLevel": any(token in title.lower() for token in ("junior", "fresher", "intern", "thực tập")),
        "onetCode": onet_code,
        "postedAt": None,
        "rawUrl": url,
        "description": description,
        "skills": skills,
    }


def infer_onet_and_skills(text: str) -> tuple[str, list[str]]:
    lowered = text.lower()
    if any(token in lowered for token in ("marketing", "seo", "campaign", "thị trường")):
        return "13-1161.00", ["Market Research", "SEO Analytics", "Communication"]
    if any(token in lowered for token in ("developer", "software", "lập trình", "java", "frontend", "backend")):
        return "15-1252.00", ["Java", "Web Development", "Critical Thinking"]
    return "15-2051.00", ["Python", "SQL", "Machine Learning", "Data Analysis"]


def fallback_jobs() -> list[dict[str, Any]]:
    return [
        to_job("Data Analyst - TopCV sample", "https://www.topcv.vn/job-sample-data-analyst", "Phân tích dữ liệu, SQL, dashboard và insight kinh doanh."),
        to_job("Software Developer - TopCV sample", "https://www.topcv.vn/job-sample-software-developer", "Phát triển ứng dụng web, Java, API và kiểm thử."),
        to_job("Marketing Data Analyst - TopCV sample", "https://www.topcv.vn/job-sample-marketing-analyst", "Phân tích chiến dịch marketing, SEO analytics và hành vi khách hàng."),
    ]


def main() -> int:
    run = http_json("POST", "/api/internal/v1/crawler/runs", {"sourceName": "TopCV"})
    run_id = run["data"]["id"]
    try:
        try:
            jobs = parse_jobs(fetch_topcv_html())
        except URLError:
            jobs = []
        if not jobs:
            jobs = fallback_jobs()
        result = http_json("POST", "/api/internal/v1/jobs/batch", {"crawlRunId": run_id, "jobs": jobs})
        http_json("POST", "/api/internal/v1/market-signals/recalculate")
        http_json("PUT", f"/api/internal/v1/crawler/runs/{run_id}", {"status": "COMPLETED"})
        print(json.dumps({"crawlRunId": run_id, "import": result["data"], "jobsSent": len(jobs)}, ensure_ascii=False))
        return 0
    except Exception as exc:
        http_json("PUT", f"/api/internal/v1/crawler/runs/{run_id}", {"status": "FAILED", "errorMessage": str(exc)})
        print(f"crawler failed: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
