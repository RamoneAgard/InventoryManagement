

// Functions for retrieving and sending product table data
function setListenerForPages(mutations, container, nextPageId, prevPageId){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const nextLink = document.getElementById(nextPageId);
            nextLink.addEventListener("click", function(event){
                event.preventDefault();
                getNextProductPage(nextLink, container);
            });
            const prevLink = document.getElementById(prevPageId);
            prevLink.addEventListener("click", function(event){
                event.preventDefault();
                getNextProductPage(prevLink, container);
            });
            break;
        }
    }
}

function setListenerForProductDelete(mutations, container){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const deleteBtnList = document.getElementsByClassName("deleteBtn");
            for(const el of deleteBtnList){
                el.addEventListener("click", function(event){
                    event.preventDefault();
                    deleteProductInTable(el, container);
                })
            }
            break;
        }
    }
}

function getProductFilterData(form, resultContainer) {
    const url = form.getAttribute("action");
    const filterData = new FormData(filterForm);
    const callback = (response) => {
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "POST", url, filterData);
}

function getNextProductPage(linkEl, resultContainer){
    const url = linkEl.getAttribute("link");
    const callback = (response) => {
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "GET", url);
}

function getInitialTable(url, resultContainer){
    const callback = (response) => {
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "GET", url);
}

function deleteProductInTable(deleteBtn, resultContainer){
    const url = deleteBtn.getAttribute("link");
    const callback = (response) => {
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "GET", url);
}


// Function to send data request
function fetchData(callback, method, url, data){
    const xhttp = new XMLHttpRequest();

    xhttp.open(method, url, true);
    xhttp.onload = function () {
        if(xhttp.status === 200){
            console.log(xhttp.status);
            callback(xhttp.response);
            //resultEl.innerHTML = xhttp.response;
        }
        else {
            alert("Problem retrieving data:" + xhttp.status);
        }
    }
    xhttp.onerror = function (e) {
        console.log(e);
        alert("Problem retrieving data:" + e);
    }

    if(data){
        xhttp.send(data);
    }
    else{
        xhttp.send();
    }
}
