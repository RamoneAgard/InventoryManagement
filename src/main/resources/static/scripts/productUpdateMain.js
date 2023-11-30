
const formDiv = document.getElementById("formContainer");
const tableDiv = document.getElementById("tableContainer");
const filterForm = document.getElementById('productFilter');

// Set-up for retrieving the update form
var formObserver;
if(formDiv != null){

    var tableUpdateAfterSubmit = () => {};
    if(tableDiv != null && filterForm != null){
        tableUpdateAfterSubmit = () =>{
            getDataFilterData(filterForm, tableDiv);
        };
    }

    document.addEventListener("DOMContentLoaded", function(){
        setFormEvent(formDiv, tableUpdateAfterSubmit, "dataForm");
    });

    formObserver = new MutationObserver(function(mutations, observer){
        setListenerForDataForm(mutations, formDiv, tableUpdateAfterSubmit, "dataForm");
        setListenerForClearUpdate(mutations, formDiv, "clearUpdate");
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
        let initUrl = "";
        if(filterForm != null){
            initUrl = filterForm.getAttribute("action");
            filterForm.addEventListener("submit", function(event){
                event.preventDefault();
                getDataFilterData(filterForm, tableDiv);
            });
        }
        getInitialTable(initUrl, tableDiv);
    });

    pageObserver = new MutationObserver(function(mutations, observer){
        setListenerForPages(mutations, tableDiv, "nextPage", "previousPage");
        if(formDiv){
            setListenerForDataUpdates(mutations, formDiv, "updateBtn");
        }
        setListenerForDataDelete(mutations, tableDiv, "deleteBtn");
    });
    const tableObserverConfig = {
            subtree : true,
            childList : true,
    };
    pageObserver.observe(tableDiv, tableObserverConfig);
}

