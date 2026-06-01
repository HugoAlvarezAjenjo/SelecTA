/**
 * SelecTA - Global Accessibility Utilities
 * 
 * Provides:
 * 1. ARIA Live Announcer - for screen reader announcements of dynamic changes
 * 2. Focus Trap - to keep keyboard focus within modals/dialogs
 * 3. Escape key handling for open dialogs
 */

// ═══════════════════════════════════════════════════════════════
// ARIA LIVE ANNOUNCER
// Use: announce('Voto registrado') to notify screen readers
// ═══════════════════════════════════════════════════════════════
function announce(message, priority) {
    const announcer = document.getElementById('aria-live-announcer');
    if (!announcer) return;

    // Change priority if needed (polite or assertive)
    if (priority === 'assertive') {
        announcer.setAttribute('aria-live', 'assertive');
    } else {
        announcer.setAttribute('aria-live', 'polite');
    }

    // Clear and set message (needed for repeated announcements)
    announcer.textContent = '';
    setTimeout(function() {
        announcer.textContent = message;
    }, 100);
}

// ═══════════════════════════════════════════════════════════════
// FOCUS TRAP
// Keeps Tab/Shift+Tab within a container (for modals)
// Usage: 
//   const trap = createFocusTrap(modalElement);
//   trap.activate();
//   trap.deactivate();
// ═══════════════════════════════════════════════════════════════
function createFocusTrap(container) {
    const FOCUSABLE_SELECTORS = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

    function getFocusableElements() {
        return Array.from(container.querySelectorAll(FOCUSABLE_SELECTORS))
            .filter(el => !el.closest('[hidden]') && !el.closest('.hidden') && el.offsetParent !== null);
    }

    function handleKeydown(e) {
        if (e.key !== 'Tab') return;

        const focusable = getFocusableElements();
        if (focusable.length === 0) return;

        const firstFocusable = focusable[0];
        const lastFocusable = focusable[focusable.length - 1];

        if (e.shiftKey) {
            // Shift+Tab: If on first element, go to last
            if (document.activeElement === firstFocusable) {
                e.preventDefault();
                lastFocusable.focus();
            }
        } else {
            // Tab: If on last element, go to first
            if (document.activeElement === lastFocusable) {
                e.preventDefault();
                firstFocusable.focus();
            }
        }
    }

    return {
        activate: function() {
            container.addEventListener('keydown', handleKeydown);
            // Focus first focusable element
            const focusable = getFocusableElements();
            if (focusable.length > 0) {
                setTimeout(function() { focusable[0].focus(); }, 50);
            }
        },
        deactivate: function() {
            container.removeEventListener('keydown', handleKeydown);
        }
    };
}

// ═══════════════════════════════════════════════════════════════
// AUTO-ENHANCE: Add aria-hidden to all Font Awesome icons 
// that don't already have it (catch any missed ones)
// ═══════════════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', function() {
    // Auto-hide decorative Font Awesome icons that weren't marked
    document.querySelectorAll('i.fas, i.far, i.fab').forEach(function(icon) {
        if (!icon.hasAttribute('aria-hidden') && !icon.hasAttribute('aria-label') && !icon.hasAttribute('role')) {
            icon.setAttribute('aria-hidden', 'true');
        }
    });
});
