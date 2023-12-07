
const catFormDiv = document.getElementById("categoryFormContainer");
const catTableDiv = document.getElementById("categoryTableContainer");
const catFilterForm = document.getElementById("catFilterForm");
const catDataFormId = "categoryDataForm";
const catDataFormClearBtnId = "categoryClearBtn";
const catTableUpdateBtnClassName = "catUpdateBtn";
const catTableDeleteBtnClassName = "catDeleteBtn";
const catTableActivateBtnClassName = "catActivateBtn";
const catTableNextBtnId = "catNextPage";
const catTablePreviousBtnId = "catPreviousPage";


const volFormDiv = document.getElementById("volumeFormContainer");
const volTableDiv = document.getElementById("volumeTableContainer");
const volFilterForm = document.getElementById("volFilterForm");
const volDataFormId = "volumeDataForm";
const volDataFormClearBtnId = "volumeClearBtn";
const volTableUpdateBtnClassName = "volUpdateBtn";
const volTableDeleteBtnClassName = "volDeleteBtn";
const volTableActivateBtnClassName = "volActivateBtn";
const volTableNextBtnId = "volNextPage";
const volTablePreviousBtnId = "volPreviousPage";

//set-up for the category update form
var catFormObserver;
if(catFormDiv != null){

    var catTableUpdateAfterSubmit = () =>{};
    if(catTableDiv != null){
        catTableUpdateAfterSubmit = () => {
            getDataFilterData(catFilterForm, catTableDiv);
        };
    }

    document.addEventListener("DOMContentLoaded", function(){
        setFormEvent(catFormDiv, catTableUpdateAfterSubmit, catDataFormId);
    });

    catFormObserver = new MutationObserver(function(mutations, observer){
        setListenerForDataForm(mutations, catFormDiv, catTableUpdateAfterSubmit, catDataFormId);
        setListenerForClearUpdate(mutations, catFormDiv, catDataFormClearBtnId);
    });
    const catFormObserverConfig = {
        subtree: true,
        childList: true
    };
    catFormObserver.observe(catFormDiv, catFormObserverConfig);
}


//set-up for the category data table
var catTableObserver;
if(catTableDiv != null){

    document.addEventListener("DOMContentLoaded", function(){
        let initUrl = "";
        if(catFilterForm != null){
            initUrl = catFilterForm.getAttribute("action");
            catFilterForm.addEventListener("submit", function(event){
                event.preventDefault();
                getDataFilterData(catFilterForm, catTableDiv);
            });
        }
        getInitialTable(initUrl, catTableDiv);
    });

    catTableObserver = new MutationObserver(function(mutations, observer){
        setListenerForPages(mutations, catTableDiv, catTableNextBtnId, catTablePreviousBtnId);
        if(catFormDiv){
            setListenerForDataUpdates(mutations, catFormDiv, catTableUpdateBtnClassName);
        }
        setListenerForDataDelete(mutations, catTableDiv, catTableDeleteBtnClassName);
        setListenerForDataDelete(mutations, catTableDiv, catTableActivateBtnClassName);
    });
    const catTableObserverConfig = {
        subtree : true,
        childList : true
    };
    catTableObserver.observe(catTableDiv, catTableObserverConfig);
}

// set-up for the volume update form
var volFormObserver;
if(volFormDiv != null){

    var volTableUpdateAfterSubmit = () =>{};
    if(volTableDiv != null){
        volTableUpdateAfterSubmit = () => {
            getDataFilterData(volFilterForm, volTableDiv);
        };
    }

    document.addEventListener("DOMContentLoaded", function(){
        setFormEvent(volFormDiv, volTableUpdateAfterSubmit, volDataFormId);
    });

    volFormObserver = new MutationObserver(function(mutations, observer){
        setListenerForDataForm(mutations, volFormDiv, volTableUpdateAfterSubmit, volDataFormId);
        setListenerForClearUpdate(mutations, volFormDiv, volDataFormClearBtnId);
    });
    const volFormObserverConfig = {
        subtree: true,
        childList: true
    };
    volFormObserver.observe(volFormDiv, volFormObserverConfig);
}

//set-up for the volume data table
var volTableObserver;
if(volTableDiv != null){

    document.addEventListener("DOMContentLoaded", function(){
        let initUrl = "";
        if(volFilterForm != null){
            initUrl = volFilterForm.getAttribute("action");
            volFilterForm.addEventListener("submit", function(event){
                event.preventDefault();
                getDataFilterData(volFilterForm, volTableDiv);
            });
        }
        getInitialTable(initUrl, volTableDiv);
    });

    volTableObserver = new MutationObserver(function(mutations, observer){
        setListenerForPages(mutations, volTableDiv, volTableNextBtnId, volTablePreviousBtnId);
        if(volFormDiv){
            setListenerForDataUpdates(mutations, volFormDiv, volTableUpdateBtnClassName);
        }
        setListenerForDataDelete(mutations, volTableDiv, volTableDeleteBtnClassName);
        setListenerForDataDelete(mutations, volTableDiv, volTableActivateBtnClassName);
    });
    const volTableObserverConfig = {
        subtree : true,
        childList : true
    };
    volTableObserver.observe(volTableDiv, volTableObserverConfig);
}



