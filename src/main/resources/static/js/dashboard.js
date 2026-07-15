document.addEventListener('DOMContentLoaded', function () {

    // Prevent double-submits on any form that triggers real server work.
    // The only form left is Upload -- Generate/Generate All no longer exist
    // now that PDFs are rendered on-demand at download time.
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function () {
            const button = form.querySelector('button[type="submit"]');
            if (button && !button.disabled) {
                button.dataset.originalHtml = button.innerHTML;
                button.disabled = true;
                button.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Working&hellip;';
            }
        });
    });

    // Client-side search -- filters by name, employee ID, or designation.
    // No backend round-trip needed at this scale (dozens of rows, not thousands).
    const searchInput = document.getElementById('employeeSearch');
    const tableBody = document.getElementById('employeeTableBody');
    if (searchInput && tableBody) {
        searchInput.addEventListener('input', function () {
            const term = this.value.trim().toLowerCase();
            const rows = tableBody.querySelectorAll('tr:not(#noSearchResultsRow)');
            let visibleCount = 0;

            rows.forEach(function (row) {
                const matches = row.textContent.toLowerCase().includes(term);
                row.style.display = matches ? '' : 'none';
                if (matches) visibleCount++;
            });

            const noResultsRow = document.getElementById('noSearchResultsRow');
            if (noResultsRow) {
                noResultsRow.style.display = visibleCount === 0 ? '' : 'none';
            }
        });
    }

});