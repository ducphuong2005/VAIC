export function renderSkeleton(count=4){return`<div class="skeleton-grid" role="status" aria-label="Đang tải">${'<div class="skeleton-card"><i></i><span></span><span></span></div>'.repeat(count)}</div>`}
export const renderLoadingShell=()=>`<main class="main-shell"><section class="page-content">${renderSkeleton()}</section></main>`;
