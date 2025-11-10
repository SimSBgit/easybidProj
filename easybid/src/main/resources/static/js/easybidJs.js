console.log("easybidJs.js loaded!");

function changePage(pageNo, event) {
    event && event.preventDefault();
    fetch(`/easybid/items?pageNo=${pageNo}&numOfRows=9`)
        .then(res => res.json())
        .then(data => {
            console.log("AJAX items:", data.items); // <-- 여기 확인
            updateItemGrid(data.items);
            updatePagination(pageNo, data.totalPages);
            window.history.pushState({}, '', `?pageNo=${pageNo}`);
        })
        .catch(err => console.error("fetch error", err));
}

// uuid가 작동하지 않는 코드 = innerHtml 만든 후 uuid를 세팅하지 않음.

// function updateItemGrid(items) {
// 	const grid = document.querySelector(".item-grid");
// 	grid.innerHTML = "";
// 	items.forEach(item => {
// 		const div = document.createElement("div");
// 		div.classList.add("item-card");
// 		// 여기 아래가 원인
// 		div.innerHTML = `
//             <div class="item-image">🏢</div>
//             <div class="item-content">
//                 <div class="item-title">${item.cltrNm}</div>
//                 <div class="item-info">
//                     📅입찰 시작: ${item.pbctBegnDtmFormatted}<br>
//                     📅입찰 마감: ${item.pbctClsDtmFormatted}
//                 </div>
//                 <div class="item-price">
//                     최저입찰가: ${item.minBidPrc.toLocaleString()}원
//                 </div>
//             </div>`;
// 		grid.appendChild(div);
// 	});
// }

// 간단한 HTML 이스케이프 함수 (XSS 예방)
function escapeHtml(str) {
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function updateItemGrid(items) {
    const grid = document.querySelector(".item-grid");
    grid.innerHTML = ""; // 기존 목록 비우기

    items.forEach(item => {
        const div = document.createElement("div");
        div.classList.add("item-card");

        // ★ data-uuid 반드시 설정
        if (item.uuid) {
            div.dataset.uuid = item.uuid; // <div data-uuid="...">
        } else {
            // 디버그용: 서버에서 uuid가 안오는 경우 확인
            console.warn("Item has no uuid:", item);
        }

        div.innerHTML = `
            <div class="item-image">🏢</div>
            <div class="item-content">
                <div class="item-title">${escapeHtml(item.cltrNm || '')}</div>
                <div class="item-info">
                    <div>📍 ${escapeHtml(item.cltrNm || '')}</div>
                    <div>
                        📅입찰 시작: ${escapeHtml(item.pbctBegnDtmFormatted || item.pbctBegnDtm || '')}<br>
                        📅입찰 마감: ${escapeHtml(item.pbctClsDtmFormatted || item.pbctClsDtm || '')}
                    </div>
                </div>
                <div class="item-price">
                    최저입찰가: ${item.minBidPrc != null ? Number(item.minBidPrc).toLocaleString() : '0'}원
                </div>
                <span class="item-badge">${escapeHtml(item.pbctCltrStatNm || '')}</span>
            </div>`;
        grid.appendChild(div);
    });
}


function updatePagination(currentPage, totalPages) {
    const pagination = document.querySelector(".pagination");
    pagination.innerHTML = ""; // 기존 버튼 지우기

    // 이전 버튼
    const prev = document.createElement("a");
    prev.href = "#";
    prev.textContent = "◀ 이전 ";
    prev.classList.add("pageBtn");
    if (currentPage > 1) {
        prev.onclick = (e) => changePage(currentPage - 1, e);
    } else {
        prev.classList.add("disabled"); // CSS에서 회색 처리
        prev.onclick = (e) => e.preventDefault();
    }
    pagination.appendChild(prev);

    // 페이지 정보
    const span = document.createElement("span");
    span.textContent = `페이지 ${currentPage} / ${totalPages}`;
    pagination.appendChild(span);

    // 다음 버튼
    const next = document.createElement("a");
    next.href = "#";
    next.textContent = " 다음 ▶";
    next.classList.add("pageBtn");
    if (currentPage < totalPages) {
        next.onclick = (e) => changePage(currentPage + 1, e);
    } else {
        next.classList.add("disabled");
        next.onclick = (e) => e.preventDefault();
    }
    pagination.appendChild(next);
}



function searchItems() {
	alert('검색 기능이 실행됩니다.\n실제 구현 시 API를 호출하여 결과를 표시합니다.');
}

function resetSearch() {
	document.querySelectorAll('input, select').forEach(el => {
		if (el.tagName === 'SELECT') {
			el.selectedIndex = 0;
		} else {
			el.value = '';
		}
	});
}

function viewItem(uuid) {
	window.location.href=`/easybid/items/${uuid}`;
	// 실제로는 window.location.href = '/item/' + id; 등으로 이동
}

// pagenation이후 새로 생성된 .item-card는 기존 eventListener와 연결 끊김.

// document.addEventListener("click", function(e) {
//     if (e.target.closest(".item-card")) {
//         const uuid = e.target.closest(".item-card").dataset.uuid;
//         viewItem(uuid);
//     }
// });

// pagenation 이후에도 동작하도록 수정
document.addEventListener("click", function(e) {
    console.log("document click:", e.target);
    const card = e.target.closest(".item-card");
    console.log("closest .item-card ->", card);
    if (!card) return;
    const uuid = card.dataset.uuid;
    console.log("card dataset.uuid ->", uuid);
    if (uuid) {
        window.location.href = `/easybid/items/${uuid}`;
    } else {
        console.warn("⚠️ UUID가 정의되지 않음:", card);
    }
});
