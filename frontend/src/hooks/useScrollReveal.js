/* =========================================================
   useScrollReveal – IntersectionObserver hook for scroll animations
   ========================================================= */

import { useEffect, useRef, useState } from 'react';

/**
 * Custom hook that uses IntersectionObserver to detect
 * when an element scrolls into the viewport.
 *
 * @param {object} options
 * @param {number}  options.threshold  - 0–1, how much of the element must be visible (default 0.15)
 * @param {string}  options.rootMargin - margin around the root (default '0px 0px -60px 0px')
 * @param {boolean} options.triggerOnce - if true, stays visible after first trigger (default true)
 * @returns {{ ref: React.RefObject, isVisible: boolean }}
 */
export function useScrollReveal({
  threshold = 0.15,
  rootMargin = '0px 0px -60px 0px',
  triggerOnce = true,
} = {}) {
  const ref = useRef(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const element = ref.current;
    if (!element) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          if (triggerOnce) {
            observer.unobserve(element);
          }
        } else if (!triggerOnce) {
          setIsVisible(false);
        }
      },
      { threshold, rootMargin },
    );

    observer.observe(element);

    return () => observer.disconnect();
  }, [threshold, rootMargin, triggerOnce]);

  return { ref, isVisible };
}
