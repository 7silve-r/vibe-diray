import type { ObjectDirective } from 'vue';
const listeners = new WeakMap<HTMLElement, () => void>();
export const tilt: ObjectDirective<HTMLElement> = {
  mounted(el) {
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)');
    const fine = window.matchMedia('(hover: hover) and (pointer: fine)');
    function move(e: PointerEvent) {
      if (reduced.matches || !fine.matches) return;
      const r = el.getBoundingClientRect();
      el.style.setProperty('--rx', `${(-(e.clientY - r.top - r.height / 2) / r.height) * 3}deg`);
      el.style.setProperty('--ry', `${((e.clientX - r.left - r.width / 2) / r.width) * 3}deg`);
    }
    function reset() {
      el.style.removeProperty('--rx');
      el.style.removeProperty('--ry');
    }
    el.addEventListener('pointermove', move);
    el.addEventListener('pointerleave', reset);
    listeners.set(el, () => {
      el.removeEventListener('pointermove', move);
      el.removeEventListener('pointerleave', reset);
    });
  },
  unmounted(el) {
    listeners.get(el)?.();
    listeners.delete(el);
  },
};
