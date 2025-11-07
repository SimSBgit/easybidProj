console.log("easybidJs.js loaded!");

function changePage(pageNo, event) {
	event.preventDefault(); // 링크 기본 이동 막기
	fetch(`/easybid/items?pageNo=${pageNo}&numOfRows=9`)
	.then(res => res.json())
		.then(data => {
			updateItemGrid(data.items);
			updatePagination(pageNo, data.totalPages); // ✅ 여기서 다시 버튼 + 텍스트 갱신
			window.history.pushState({}, '', `?pageNo=${pageNo}`);
		});
}


function updateItemGrid(items) {
	const grid = document.querySelector(".item-grid");
	grid.innerHTML = "";
	items.forEach(item => {
		const div = document.createElement("div");
		div.classList.add("item-card");
		div.innerHTML = `
            <div class="item-image">🏢</div>
            <div class="item-content">
                <div class="item-title">${item.cltrNm}</div>
                <div class="item-info">
                    📅입찰 시작: ${item.pbctBegnDtmFormatted}<br>
                    📅입찰 마감: ${item.pbctClsDtmFormatted}
                </div>
                <div class="item-price">
                    최저입찰가: ${item.minBidPrc.toLocaleString()}원
                </div>
            </div>`;
		grid.appendChild(div);
	});
}

function updatePagination(currentPage, totalPages) {
	const pagination = document.querySelector(".pagination");
	pagination.innerHTML = ""; // 기존 버튼 지우기

	// 이전 버튼
	if (currentPage > 1) {
		const prev = document.createElement("a");
		prev.href = "#";
		prev.textContent = "◀ 이전 ";
		prev.onclick = (e) => changePage(currentPage - 1, e);
		pagination.appendChild(prev);
	}

	// 페이지 정보
	const span = document.createElement("span");
	span.textContent = `페이지 ${currentPage} / ${totalPages}`;
	pagination.appendChild(span);

	// 다음 버튼
	if (currentPage < totalPages) {
		const next = document.createElement("a");
		next.href = "#";
		next.textContent = " 다음 ▶";
		next.onclick = (e) => changePage(currentPage + 1, e);
		pagination.appendChild(next);
	}
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

function viewItem(id) {
	alert(`물건 상세 페이지로 이동합니다.\n물건 ID: ${id}`);
	// 실제로는 window.location.href = '/item/' + id; 등으로 이동
}