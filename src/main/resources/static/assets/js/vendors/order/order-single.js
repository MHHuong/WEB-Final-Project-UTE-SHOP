(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const url = new URL(window.location.href);
    const orderId = url.searchParams.get('orderId');

    const orderHeader = document.getElementById('orderHeader');
    const buyerBlock = document.getElementById('buyerBlock');
    const itemsTbody = document.getElementById('itemsTbody');
    const priceBox = document.getElementById('priceBox');
    const actionBox = document.getElementById('actionBox');

    const fmt = v => (v ?? 0).toLocaleString('vi-VN') + ' VND';
    const d = iso => iso ? new Date(iso).toLocaleString() : '';

    function badge(st) {
        switch (st) {
            case 'NEW':
                return 'bg-warning';
            case 'CONFIRMED':
                return 'bg-success';
            case 'CANCELLED':
                return 'bg-secondary';
            case 'SHIPPING':
                return 'bg-info';
            case 'DELIVERED':
                return 'bg-info';
            case 'RECEIVED':
                return 'bg-primary';
            case 'RETURNED':
                return 'bg-dark';
            default:
                return 'bg-light text-dark';
        }
    }

    async function load() {
        const res = await fetch(`${BASE}/api/shop/orders/${orderId}`, {
            headers: {'Authorization': 'Bearer ' + token}
        });
        if (!res.ok) {
            alert('Không tải được đơn');
            return;
        }
        const o = await res.json();

        orderHeader.innerHTML = `
      <div class="d-flex justify-content-between align-items-center">
        <div>
          <h4 class="mb-1">Order #${o.orderId}</h4>
          <div class="text-muted">Created: ${d(o.createdAt)}</div>
        </div>
        <div><span class="badge ${badge(o.status)}">${o.status}</span></div>
      </div>`;

        buyerBlock.innerHTML = `
      <div class="fw-semibold">${o.customerName || ''}</div>
      <div class="text-muted small">${o.customerEmail || ''}</div>
      ${o.shippingAddress ? `<div>${o.shippingAddress}</div>` : ''}
      ${o.couponCode ? `<div class="mt-1">Coupon: <span class="badge bg-primary">${o.couponCode}</span></div>` : ''}
    `;

        itemsTbody.innerHTML = (o.items || []).map(it => {
            // hiển thị “giá gốc → sau KM → sau coupon” (nếu có)
            const unitOrg = it.unitPrice || 0;
            const disc = it.itemDiscount || 0;
            const unitAfterPromo = (unitOrg - (disc / (it.quantity || 1)));
            const afterCoupon = unitAfterPromo - ((it.couponShare || 0) / (it.quantity || 1));

            const unitHtml = (disc > 0 || (it.couponShare || 0) > 0)
                ? `<span class="text-decoration-line-through me-1">${fmt(unitOrg)}</span>
           <span class="text-danger me-1">${fmt(unitAfterPromo)}</span>
           ${(it.couponShare || 0) > 0 ? `<span class="text-success">→ ${fmt(afterCoupon)}</span>` : ''}`
                : fmt(unitOrg);

            const lineOrg = (it.unitPrice || 0) * (it.quantity || 0);
            const lineAfterPromo = lineOrg - (it.itemDiscount || 0);
            const lineFinal = lineAfterPromo - (it.couponShare || 0);

            const thumb = it.thumbUrl
                ? `<img src="${it.thumbUrl}" alt="" class="rounded me-2" style="width:48px;height:48px;object-fit:cover;">`
                : '';

            return `<tr>
        <td>
          <div class="d-flex align-items-center">
            ${thumb}
            <div>
              <a class="fw-semibold" href="${BASE}/shop/product/single-product?productId=${it.productId}">${it.productName || ('#' + it.productId)}</a>
            </div>
          </div>
        </td>
        <td>${it.quantity}</td>
        <td>${unitHtml}</td>
        <td class="fw-semibold">
          ${(disc > 0 || (it.couponShare || 0) > 0)
                ? `<span class="text-decoration-line-through me-1">${fmt(lineOrg)}</span>
               <span class="text-danger me-1">${fmt(lineAfterPromo)}</span>
               ${(it.couponShare || 0) > 0 ? `<span class="text-success">→ ${fmt(lineFinal)}</span>` : ''}`
                : fmt(lineOrg)
            }
        </td>
      </tr>`;
        }).join('');

        priceBox.innerHTML = `
      <div>Subtotal (original): <span class="float-end">${fmt(o.subtotalOriginal)}</span></div>
      <div>Promotion discount: <span class="float-end text-danger">- ${fmt(o.promotionDiscount)}</span></div>
      <div>Coupon discount: <span class="float-end text-success">- ${fmt(o.couponDiscount)}</span></div>
      <div>Shipping fee: <span class="float-end">${fmt(o.shippingFee)}</span></div>
      <hr class="my-2">
      <div class="fw-bold">Total: <span class="float-end">${fmt(o.totalAmount)}</span></div>
    `;

        if (o.status === 'NEW') {
            actionBox.innerHTML = `
        <button id="btnConfirm" class="btn btn-success me-2">Confirm</button>
        <button id="btnCancel" class="btn btn-outline-secondary">Cancel</button>
      `;
            document.getElementById('btnConfirm').onclick = () => changeStatus('CONFIRMED');
            document.getElementById('btnCancel').onclick = () => changeStatus('CANCELLED');
        } else {
            actionBox.innerHTML = '';
        }
    }

    async function changeStatus(next) {
        const res = await fetch(`${BASE}/api/shop/orders/${orderId}/status`, {
            method: 'PATCH',
            headers: {'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'},
            body: JSON.stringify({status: next})
        });
        if (!res.ok) {
            alert('Cập nhật thất bại');
            return;
        }
        await load();
    }

    load();
})();
