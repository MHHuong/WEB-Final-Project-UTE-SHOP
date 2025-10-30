document.addEventListener("DOMContentLoaded", function () {
    // Lấy context-path do Thymeleaf render vào <meta>
    const contextMeta = document.querySelector('meta[name="app:ctx"]');
    const contextPath = (contextMeta?.content || '/').replace(/\/?$/, '/'); // đảm bảo có dấu '/'

    if (window.jQuery && window.jQuery.fn.slick) {
        $(".deals-slider").slick({
            infinite: true,
            slidesToShow: 3,
            slidesToScroll: 3,
            arrows: true,
            dots: false,
            prevArrow: '<button type="button" class="slick-prev"><i class="bi bi-chevron-left"></i></button>',
            nextArrow: '<button type="button" class="slick-next"><i class="bi bi-chevron-right"></i></button>',
            autoplay: true,
            autoplaySpeed: 10000,
            responsive: [
                {breakpoint: 1200, settings: {slidesToShow: 2, slidesToScroll: 2}},
                {breakpoint: 768, settings: {slidesToShow: 1, slidesToScroll: 1}},
            ],
        });
    } else {
        console.error("jQuery hoặc Slick Slider chưa được nạp. Slider 'deals-slider' sẽ không hoạt động.");
    }

    const TOKEN_KEY = "authToken";
    const urlParams = new URLSearchParams(window.location.search);
    const isLogout = urlParams.get("logout");

    if (isLogout === "true") {
        console.log("Phát hiện logout=true, đang dọn dẹp Local Storage...");
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem("userId");
        localStorage.removeItem("userEmail");
        localStorage.removeItem("userFullName");
        localStorage.removeItem("userRole");
        // Dùng contextPath thay vì "/"
        window.history.replaceState({}, document.title, contextPath);
    }

    const token = localStorage.getItem(TOKEN_KEY);

    if (token) {
        // API luôn prefix contextPath
        const apiUrl = contextPath + "api/auth/me";

        fetch(apiUrl, {
            method: "GET",
            headers: {Authorization: "Bearer " + token},
        })
            .then((response) => {
                if (!response.ok) throw new Error("Token không hợp lệ");
                return response.json();
            })
            .then(data => {
                document.getElementById('nav-cart-anonymous')?.classList.add('d-none');
                document.getElementById('nav-user-anonymous')?.classList.add('d-none');
                document.getElementById('nav-cart-authenticated')?.classList.remove('d-none');
                document.getElementById('nav-user-authenticated')?.classList.remove('d-none');

                const shopLink = document.getElementById('nav-shop-link');
                if (shopLink) {
                    shopLink.href = data.shop ? (contextPath + 'shop') : (contextPath + 'shop/register');
                }

                localStorage.setItem('userId', String(data.userId ?? ''));
                localStorage.setItem('userEmail', data.email ?? '');
                localStorage.setItem('userFullName', data.fullName ?? '');
                localStorage.setItem('userRole', data.role ?? '');

                const role = (data.role || '').replace(/^ROLE_/, '').toUpperCase();  // USER | SELLER | SHIPPER

                const shipperItem = document.getElementById('nav-shipper-item');
                const shipperLink = document.getElementById('nav-shipper-link');
                const shopItem = document.getElementById('nav-shop-item');

                if (shipperItem && shipperLink) {
                    if (role === 'SHIPPER') {
                        shipperLink.href = contextPath + 'shipper';
                        shipperItem.classList.remove('d-none');
                        if (shopItem) shopItem.classList.add('d-none');
                    } else if (role === 'SELLER') {
                        shipperItem.classList.add('d-none');
                        if (shopItem) shopItem.classList.remove('d-none');
                    } else if (role === 'USER') {
                        shipperLink.href = contextPath + 'shipper/register';
                        shipperItem.classList.remove('d-none');
                        if (shopItem) shopItem.classList.remove('d-none');
                    } else {
                        shipperItem.classList.add('d-none');
                    }
                }

                const logoutButton = document.getElementById('nav-logout-button');
                if (logoutButton) {
                    logoutButton.addEventListener('click', function (e) {
                        e.preventDefault();
                        localStorage.removeItem(TOKEN_KEY);
                        localStorage.removeItem('userId');
                        localStorage.removeItem('userEmail');
                        localStorage.removeItem('userFullName');
                        localStorage.removeItem('userRole');
                        window.location.href = contextPath; // dùng contextPath
                    });
                }
            })
            .catch((error) => {
                console.error("Lỗi khi gọi API /me hoặc token không hợp lệ:", error);
                localStorage.removeItem(TOKEN_KEY);
            });
    } else {
        console.log("Không tìm thấy token, người dùng chưa đăng nhập.");
    }
});