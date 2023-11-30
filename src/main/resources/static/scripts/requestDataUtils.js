// Functions for retrieving and sending from data
function setListenerForDataForm(mutations, container, tableUpdateFunction, formId){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            setFormEvent(container, tableUpdateFunction, formId);
            break;
        }
    }
}

function setListenerForDataUpdates(mutations, container, btnClass){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const updateBtnList = document.getElementsByClassName(btnClass);
            for(const el of updateBtnList){
                el.addEventListener("click", function(event){
                    event.preventDefault();
                    getUpdateFormForData(el, container);
                })
            }
            break;
        }
    }
}

function setListenerForClearUpdate(mutations, container, btnId){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const clearBtn = document.getElementById(btnId);
            if(clearBtn){
                clearBtn.addEventListener("click", function(event){
                    event.preventDefault();
                    getUpdateFormForData(clearBtn, container);
                });
            }
            break;
        }
    }
}

function setFormEvent(resultContainer, tableUpdateFunction, formId){
    const dataForm = document.getElementById(formId);
    dataForm.addEventListener("submit", function(event){
        event.preventDefault();
        submitDataForm(dataForm, resultContainer, tableUpdateFunction);
    })
}

function submitDataForm(form, resultContainer, tableUpdateFunction){
    const url = form.getAttribute("action");
    const formData = new FormData(form);
    const callback = (response) => {
        tableUpdateFunction();
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "POST", url, formData);
}

function getUpdateFormForData(updateBtn, resultContainer){
    const url = updateBtn.getAttribute("link");
    const callback = (response) => {
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "GET", url);
}

// Functions for retrieving and sending table data
function setListenerForPages(mutations, container, nextPageId, prevPageId){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const nextLink = document.getElementById(nextPageId);
            nextLink.addEventListener("click", function(event){
                event.preventDefault();
                getNextDataPage(nextLink, container);
            });
            const prevLink = document.getElementById(prevPageId);
            prevLink.addEventListener("click", function(event){
                event.preventDefault();
                getNextDataPage(prevLink, container);
            });
            break;
        }
    }
}

function setListenerForDataDelete(mutations, container, btnClass){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const deleteBtnList = document.getElementsByClassName(btnClass);
            for(const el of deleteBtnList){
                el.addEventListener("click", function(event){
                    event.preventDefault();
                    deleteDataInTable(el, container);
                })
            }
            break;
        }
    }
}

function getDataFilterData(form, resultContainer) {
    const url = form.getAttribute("action");
    const filterData = new FormData(form);
    const callback = (response) => {
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "POST", url, filterData);
}

function getNextDataPage(linkEl, resultContainer){
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

function deleteDataInTable(deleteBtn, resultContainer){
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
        if(xhttp.status === 200 || xhttp.status === 201){
            console.log(xhttp.status);
            if(/login$/.test(xhttp.responseURL)){
                console.log(window.location);
                window.location.reload();
            } else {
                callback(xhttp.response);
            }
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
