import{escapeHtml}from'../core/formatters.js';
export const toMiniGameViewModel=(g={})=>({id:encodeURIComponent(g.id||''),title:escapeHtml(g.title||'Mini-game'),description:escapeHtml(g.description||''),type:escapeHtml(g.gameType||'Khám phá'),minutes:Number(g.estimatedMinutes)||0});
