
// Set-up for retrieving the update form
const formDiv = document.getElementById("formContainer");
const tableDiv = document.getElementById("tableContainer");
const filterForm = document.getElementById('productFilter');

var formObserver;
if(formDiv != null){

    var tableUpdateAfterSubmit = () => {};
    if(tableDiv != null && filterForm != null){
        tableUpdateAfterSubmit = () =>{
            getProductFilterData(filterForm, tableDiv);
        };
    }

    document.addEventListener("DOMContentLoaded", function(){
        setFormEvent(formDiv, tableUpdateAfterSubmit);
    });

    formObserver = new MutationObserver(function(mutations, observer){
        setListenerForProductForm(mutations, formDiv, tableUpdateAfterSubmit);
        setListenerForClearUpdate(mutations, formDiv);
    });
    const formObserverConfig = {
        subtree : true,
        childList : true,
    };
    formObserver.observe(formDiv, formObserverConfig);
}


// Set-up for retrieving the product table
var pageObserver;
if(tableDiv != null){

    document.addEventListener("DOMContentLoaded", function(event){
        getInitialTable("/products/table", tableDiv);

        if(filterForm != null){
            filterForm.addEventListener("submit", function(event){
                event.preventDefault();
                getProductFilterData(filterForm, tableDiv);
            });
        }
    });

    pageObserver = new MutationObserver(function(mutations, observer){
        setListenerForPages(mutations, tableDiv, "nextPage", "previousPage");
        if(formDiv){
            setListenerForProductUpdates(mutations, formDiv);
        }
        setListenerForProductDelete(mutations, tableDiv);
    });
    const tableObserverConfig = {
            subtree : true,
            childList : true,
        };
    pageObserver.observe(tableDiv, tableObserverConfig);
}


// Functions for retrieving and sending product form data
function setListenerForProductForm(mutations, container, tableUpdateFunction){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            setFormEvent(container, tableUpdateFunction);
            break;
        }
    }
}

function setListenerForProductUpdates(mutations, container){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const updateBtnList = document.getElementsByClassName("updateBtn");
            for(const el of updateBtnList){
                el.addEventListener("click", function(event){
                    event.preventDefault();
                    getUpdateFormForProduct(el, container);
                })
            }
            break;
        }
    }

}

function setListenerForClearUpdate(mutations, container){
    for(const mutation of mutations){
        if(mutation.type === "childList"){
            const clearBtn = document.getElementById("clearUpdate");
            if(clearBtn){
                clearBtn.addEventListener("click", function(event){
                    event.preventDefault();
                    getUpdateFormForProduct(clearBtn, container);
                });
            }
            break;
        }
    }
}

function setFormEvent(resultContainer, tableUpdateFunction){
    const productForm = document.getElementById("productForm");
    productForm.addEventListener("submit", function(event){
        event.preventDefault();
        submitProductForm(productForm, resultContainer, tableUpdateFunction);
    })
}

function submitProductForm(form, resultContainer, tableUpdateFunction){
    const url = form.getAttribute("action");
    const formData = new FormData(form);
    const callback = (response) => {
        tableUpdateFunction();
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "POST", url, formData);
}

function getUpdateFormForProduct(updateBtn, resultContainer){
    const url = updateBtn.getAttribute("link");
    const callback = (response) => {
        resultContainer.innerHTML = response;
    };
    fetchData(callback, "GET", url);
}
