// Get the context path from the current URL
const getContextPath = () => {
    const pathParts = window.location.pathname.split('/');
    return pathParts[1] === 'UTE_SHOP' ? '/UTE_SHOP' : '';
};

export const CONTEXT_PATH = getContextPath();
export const API_BASE_URL = `${CONTEXT_PATH}/api`;

