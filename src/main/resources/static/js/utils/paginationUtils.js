// Pagination utility functions

/**
 * Render pagination controls
 * @param {Object} paginationData - { currentPage, totalPages, size, totalElements }
 * @param {Function} onPageChange - Callback function when page changes (receives page number)
 * @param {string} containerId - ID of the container to render pagination
 */
export function renderPagination(paginationData, onPageChange, containerId = 'pagination-container') {
    const { currentPage, totalPages, size, totalElements } = paginationData;
    const container = document.getElementById(containerId);

    if (!container) {
        console.error(`Container with ID "${containerId}" not found`);
        return;
    }

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let paginationHTML = '<nav aria-label="Page navigation"><ul class="pagination justify-content-center mb-0">';

    // Previous button
    const isPrevDisabled = currentPage === 0;
    paginationHTML += `
        <li class="page-item ${isPrevDisabled ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage - 1}" ${isPrevDisabled ? 'tabindex="-1" aria-disabled="true"' : ''}>
                <i class="bi bi-chevron-left"></i>
            </a>
        </li>
    `;

    // Calculate visible page range
    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    // Adjust start page if we're near the end
    if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    // First page + ellipsis
    if (startPage > 0) {
        paginationHTML += `
            <li class="page-item">
                <a class="page-link" href="#" data-page="0">1</a>
            </li>
        `;
        if (startPage > 1) {
            paginationHTML += '<li class="page-item disabled"><span class="page-link">...</span></li>';
        }
    }

    // Visible page numbers
    for (let i = startPage; i <= endPage; i++) {
        const isActive = i === currentPage;
        paginationHTML += `
            <li class="page-item ${isActive ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">
                    ${i + 1}
                    ${isActive ? '<span class="visually-hidden">(current)</span>' : ''}
                </a>
            </li>
        `;
    }

    // Ellipsis + last page
    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            paginationHTML += '<li class="page-item disabled"><span class="page-link">...</span></li>';
        }
        paginationHTML += `
            <li class="page-item">
                <a class="page-link" href="#" data-page="${totalPages - 1}">${totalPages}</a>
            </li>
        `;
    }

    // Next button
    const isNextDisabled = currentPage >= totalPages - 1;
    paginationHTML += `
        <li class="page-item ${isNextDisabled ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage + 1}" ${isNextDisabled ? 'tabindex="-1" aria-disabled="true"' : ''}>
                <i class="bi bi-chevron-right"></i>
            </a>
        </li>
    `;

    paginationHTML += '</ul></nav>';

    container.innerHTML = paginationHTML;

    // Add click event listeners
    container.querySelectorAll('.page-link[data-page]').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();

            // Don't do anything if disabled
            const parentLi = this.closest('.page-item');
            if (parentLi.classList.contains('disabled') || parentLi.classList.contains('active')) {
                return;
            }

            const page = parseInt(this.getAttribute('data-page'));
            if (!isNaN(page) && page >= 0 && page < totalPages) {
                onPageChange(page);

                // Scroll to top of container smoothly
                const targetElement = container.closest('.card-body') || container.closest('.card') || document.body;
                targetElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });
}

/**
 * Show page info (e.g., "Hiển thị 1-5 trong tổng số 20")
 * @param {Object} pageInfo - { currentPage, size, totalElements }
 * @param {string} containerId - ID of the container to show info
 */
export function showPageInfo(pageInfo, containerId = 'page-info-container') {
    const { currentPage, size, totalElements } = pageInfo;
    const container = document.getElementById(containerId);

    if (!container) {
        console.error(`Container with ID "${containerId}" not found`);
        return;
    }

    if (totalElements === 0) {
        container.innerHTML = '<p class="text-muted mb-0 small">Không có dữ liệu</p>';
        return;
    }

    const start = currentPage * size + 1;
    const end = Math.min((currentPage + 1) * size, totalElements);

    container.innerHTML = `
        <p class="text-muted mb-0 small">
            Hiển thị <strong>${start}</strong> - <strong>${end}</strong> 
            trong tổng số <strong>${totalElements}</strong>
        </p>
    `;
}

/**
 * Create pagination state object
 * @param {number} currentPage - Current page number (0-indexed)
 * @param {number} size - Items per page
 * @param {number} totalElements - Total number of items
 * @returns {Object} Pagination state object
 */
export function createPaginationState(currentPage, size, totalElements) {
    const totalPages = Math.ceil(totalElements / size) || 1;

    return {
        currentPage: Math.max(0, Math.min(currentPage, totalPages - 1)),
        size: Math.max(1, size),
        totalPages,
        totalElements: Math.max(0, totalElements)
    };
}

export function paginateArray(array, page = 0, size = 10) {
    const totalElements = array.length;
    const totalPages = Math.ceil(totalElements / size) || 1;
    const currentPage = Math.max(0, Math.min(page, totalPages - 1));

    const start = currentPage * size;
    const end = Math.min(start + size, totalElements);
    const content = array.slice(start, end);

    return {
        content,
        pagination: {
            currentPage,
            size,
            totalPages,
            totalElements
        }
    };
}
