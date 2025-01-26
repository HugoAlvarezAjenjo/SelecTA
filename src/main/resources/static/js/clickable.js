document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.clickable').forEach(function (element) {
        element.addEventListener('click', function () {
            const href = this.getAttribute('data-href');
            if (href) {
                window.location.href = href;
            }
        });
    });
});
