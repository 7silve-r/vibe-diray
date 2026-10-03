import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';
const md = new MarkdownIt({ html: false, linkify: true, breaks: true, typographer: true });
export function markdown(value: string) {
  return DOMPurify.sanitize(md.render(value || ''), {
    FORBID_TAGS: ['img', 'iframe', 'style', 'script'],
    FORBID_ATTR: ['style'],
  });
}
export function excerpt(value: string) {
  return value.replace(/[#*`>_[\]]/g, '').slice(0, 160);
}
