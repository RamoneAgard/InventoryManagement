const orderFormDiv = document.getElementById("orderFormDiv");
const orderTableDiv = document.getElementById("orderTableContainer");
const orderFilterForm = document.getElementById("orderFilter");

const productTableDiv = document.getElementById("productTableContainer");
const productFilterForm = document.getElementById("productFilter");

const addFormElId = "itemAddForm";
const addFormQueryName = "code";
const dataFormElId = "dataForm";
const formDeleteBtnClassName = "formRemoveBtn";
const formClearBtnId = "clearUpdate";

const orderTableNextBtnId = "orderNextPage";
const orderTablePreviousBtnId = "orderPreviousPage";
const orderTableUpdateBtnClassName = "orderUpdateBtn";
const orderTableDeleteBtnClassName = "orderDeleteBtn";
const deleteConfirmModalId = "confirmModal";
const deleteModalBtnId = "confirmBtn";

const productTableNextBtnId = "nextPage";
const productTablePreviousBtnId = "previousPage";
const addBtnClassName = "orderAddBtn";


var orderFormObserver;
if(orderFormDiv != null){

    var orderTableUpdateAfterSubmit = () => {};
    if((orderTableDiv && orderFilterForm) && (productTableDiv && productFilterForm)){
        orderTableUpdateAfterSubmit = () => {
            getDataFilterData(orderFilterForm, orderTableDiv);
            getDataFilterData(productFilterForm, productTableDiv)
        }
    }

    document.addEventListener("DOMContentLoaded", function(){
        setAddItemEvent(orderFormDiv, addFormElId, addFormQueryName, dataFormElId);
        setFormEvent(orderFormDiv, orderTableUpdateAfterSubmit, dataFormElId);
    });

    orderFormObserver = new MutationObserver(function(mutations){
        setListenerForDataForm(mutations, orderFormDiv, orderTableUpdateAfterSubmit, dataFormElId);
        setListenerForAddForm(mutations, orderFormDiv, addFormElId, addFormQueryName, dataFormElId);
        setListenerForFormItemDelete(mutations, orderFormDiv, formDeleteBtnClassName, dataFormElId);
        setListenerForClearUpdate(mutations, orderFormDiv, formClearBtnId);
    });
    const orderFormObserverConfig = {
        subtree : true,
        childList : true,
    };
    orderFormObserver.observe(orderFormDiv, orderFormObserverConfig);

}

var orderPageObserver;
if(orderTableDiv != null){

    document.addEventListener("DOMContentLoaded", function(){
        let initUrl = "";
        if(orderFilterForm != null){
            initUrl = orderFilterForm.getAttribute("action");
            orderFilterForm.addEventListener("submit", function(event){
                event.preventDefault();
                getDataFilterData(orderFilterForm, orderTableDiv);
            });
        }
        getInitialTable(initUrl, orderTableDiv);
    });

    orderPageObserver = new MutationObserver(function(mutations, observer){
        setListenerForPages(mutations, orderTableDiv, orderTableNextBtnId, orderTablePreviousBtnId);
        if(orderFormDiv){
            setListenerForDataUpdates(mutations, orderFormDiv, orderTableUpdateBtnClassName);
        }
        setListenerForDataDelete(mutations, orderTableDiv, orderTableDeleteBtnClassName);
        setUpDeleteConfirmModal(mutations, orderTableDiv, deleteConfirmModalId, deleteModalBtnId);
    });
    const orderTableObserverConfig = {
            subtree : true,
            childList : true,
    };
    orderPageObserver.observe(orderTableDiv, orderTableObserverConfig);
}

var productPageObserver;
if(productTableDiv != null){

    document.addEventListener("DOMContentLoaded", function(event){
        let initUrl = "";
        if(productFilterForm != null){
            initUrl = productFilterForm.getAttribute("action");
            productFilterForm.addEventListener("submit", function(event){
                event.preventDefault();
                getDataFilterData(productFilterForm, productTableDiv);
            });
        }
        getInitialTable(initUrl, productTableDiv);
    });

    productPageObserver = new MutationObserver(function(mutations, observer){
        setListenerForPages(mutations, productTableDiv, productTableNextBtnId, productTablePreviousBtnId);
        if(orderFormDiv != null){
            setListenerForAddBtn(mutations, orderFormDiv, addBtnClassName, dataFormElId);
        }
    });
    const productTableObserverConfig = {
            subtree : true,
            childList : true,
    };
    productPageObserver.observe(productTableDiv, productTableObserverConfig);

}



function setListenerForAddForm(mutations, container, addFormId, urlQueryName, dataFormId){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            setAddItemEvent(container, addFormId, urlQueryName, dataFormId);
            break;
        }
    }
}

function setAddItemEvent(container, addFormId, urlQueryName, dataFormId){
    const dataFormEl = document.getElementById(dataFormId);
    const addFormEl = document.getElementById(addFormId);
    addFormEl.addEventListener("submit", function(event){
        event.preventDefault();
        addItemForItemCode(container, addFormEl, urlQueryName, dataFormEl);
    });
}

function addItemForItemCode(container, addForm, urlQueryName, dataForm){
    const addFormData = new FormData(addForm);
    const orderFormData = new FormData(dataForm);

    let url = addForm.getAttribute("action");
    url = url + "?" + urlQueryName + "=" + addFormData.get(urlQueryName);

    const callback = (response) => {
        container.innerHTML = response;
    }
    fetchData(callback, "POST", url, orderFormData);
}

function setListenerForAddBtn(mutations, container, addBtnClass, dataFormId){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const addBtnList = document.getElementsByClassName(addBtnClass);
            for(const el of addBtnList){
                el.addEventListener("click", function(event){
                    event.preventDefault();
                    addProductToForm(el, dataFormId, container);
                });
            }
            break;
        }
    }
}

function addProductToForm(addBtn, dataFormId, resultContainer){
    const orderFormData = new FormData(
        document.getElementById(dataFormId)
    );
    const url = addBtn.getAttribute("link");

    const callback = (response) => {
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "POST", url, orderFormData);
}

function setListenerForFormItemDelete(mutations, container, btnClass, dataFormId){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const dataFormEl = document.getElementById(dataFormId);
            const deleteBtnList = document.getElementsByClassName(btnClass);
            for(const el of deleteBtnList){
                el.addEventListener("click", function(){
                    event.preventDefault();
                    deleteItemInForm(el, container, dataFormEl);
                })
            }
            break;
        }
    }
}

function deleteItemInForm(deleteBtn, resultContainer, dataForm){
    const formData = new FormData(dataForm);
    const url = deleteBtn.getAttribute("link");

    const callback = (response) => {
        resultContainer.innerHTML = response;
    }
    fetchData(callback, "POST", url, formData);
}

function setUpDeleteConfirmModal(mutations, resultContainer, modalId, modalConfirmBtnId){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            modalEl = document.getElementById(modalId);
            if(modalEl){
                modalEl.addEventListener("show.bs.modal", (event) =>{
                    const triggerBtn = event.relatedTarget;
                    const linkVal = triggerBtn.getAttribute("link");
                    const modalActionBtn = document.getElementById(modalConfirmBtnId);
                    modalActionBtn.setAttribute("link", linkVal);
                });
            }
            break;
        }
    }
}



function formatValue(value){
    if(value < 10){
        return "0" + value;
    }
    return value;
}

function setDefaultDateTimePlaceHolder(inputId){
    const dateTimeEl = document.getElementById(inputId);
    const todayDT = new Date();
    let day = formatValue( todayDT.getDate() );
    let mon = formatValue( todayDT.getMonth() + 1);
    let year = todayDT.getFullYear();
    let hr = formatValue( todayDT.getHours() );
    let min = formatValue( todayDT.getMinutes() );

    let placeholderVal = year + "-" + mon + "-" + day + "T" + hr + ":" + min;
    dateTimeEl.value = placeholderVal;
}