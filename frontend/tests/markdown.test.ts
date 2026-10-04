import { describe, it, expect } from 'vitest';
import { markdown } from '../src/utils/markdown';
describe('Markdown', () => {
  it('heading', () => expect(markdown('# 今日\n\n**开心**')).toContain('<strong>开心</strong>'));
  it('script', () => {
    const html = markdown('<script>alert(1)</script>\n<img src=x onerror=alert(1)>');
    expect(html).not.toContain('<script>');
    expect(html).not.toContain('<img');
  });
  it('link', () =>
    expect(markdown('[点击](javascript:alert(1))')).not.toContain('href="javascript:'));
  it('table', () => expect(markdown('| A | B |\n|---|---|\n| 1 | 2 |')).toContain('<table>'));
});
