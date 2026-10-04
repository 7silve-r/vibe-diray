export const poems = [
  {
    author: '陶渊明',
    title: '饮酒·其五',
    lines: ['采菊东篱下，', '悠然见南山。'],
    source: 'https://zh.wikisource.org/wiki/陶淵明集/卷三',
  },
  {
    author: '苏轼',
    title: '定风波',
    lines: ['莫听穿林打叶声，', '何妨吟啸且徐行。'],
    source: 'https://zh.wikisource.org/zh/定風波_(莫聽穿林打葉聲)',
  },
  {
    author: '杜甫',
    title: '春夜喜雨',
    lines: ['随风潜入夜，', '润物细无声。'],
    source: 'https://zh.wikisource.org/zh-hans/春夜喜雨',
  },
  {
    author: '白居易',
    title: '池上二绝·其二',
    lines: ['小娃撑小艇，', '偷采白莲回。'],
    source: 'https://zh.wikisource.org/zh-hans/池上二絕',
  },
];
export function localDay(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
