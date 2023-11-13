
const catFormDiv = document.getElementById("categoryFormContainer");
const catTableDiv = document.getElementById("categoryTableContainer");

const volFormDiv = document.getElementById("volumeFormContainer");
const volTableDiv = document.getElementById("volumeTableContainer");

//set-up for the category update form
var catFormObserver;
if(catFormDiv != null){

    var catTableUpdateAfterSubmit = () =>{};
    if(catTableDiv != null){
        catTableUpdateAfterSubmit = () => {
            getInitialTable("/categories/table", catTableDiv);
        };
    }

    document.addEventListener("DOMContentLoaded", function(){
        setFormEvent(catFormDiv, catTableUpdateAfterSubmit, "categoryDataForm");
    });

    catFormObserver = new MutationObserver(function(mutations, observer){
        setListenerForDataForm(mutations, catFormDiv, catTableUpdateAfterSubmit, "categoryDataForm");
        setListenerForClearUpdate(mutations, catFormDiv, "categoryClearBtn");
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
        getInitialTable("/categories/table", catTableDiv);
    });

    catTableObserver = new MutationObserver(function(mutations, observer){
        if(catFormDiv){
            setListenerForDataUpdates(mutations, catFormDiv, "catUpdateBtn");
        }
        setListenerForDataDelete(mutations, catTableDiv, "catDeleteBtn");
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
            getInitialTable("/volumes/table", volTableDiv);
        };
    }

    document.addEventListener("DOMContentLoaded", function(){
        setFormEvent(volFormDiv, volTableUpdateAfterSubmit, "volumeDataForm");
    });

    volFormObserver = new MutationObserver(function(mutations, observer){
        setListenerForDataForm(mutations, volFormDiv, volTableUpdateAfterSubmit, "volumeDataForm");
        setListenerForClearUpdate(mutations, volFormDiv, "volumeClearBtn");
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
        getInitialTable("/volumes/table", volTableDiv);
    });

    volTableObserver = new MutationObserver(function(mutations, observer){
        if(volFormDiv){
            setListenerForDataUpdates(mutations, volFormDiv, "volUpdateBtn");
        }
        setListenerForDataDelete(mutations, volTableDiv, "volDeleteBtn");
    });
    const volTableObserverConfig = {
        subtree : true,
        childList : true
    };
    volTableObserver.observe(volTableDiv, volTableObserverConfig);
}



