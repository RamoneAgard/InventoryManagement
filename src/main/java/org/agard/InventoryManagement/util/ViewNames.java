package org.agard.InventoryManagement.util;

public final class ViewNames {

    public static final String ERROR_VIEW = "error";
    public static final String PRODUCT_VIEW = "product_list";
    public static final String PRODUCT_TABLE_FRAGMENT = "fragments/product/product_table.html :: productTable";
    public static final String PRODUCT_UPDATE_FRAGMENT = "fragments/product/product_update_form.html :: updateForm";
    public static final String ATTRIBUTE_VIEW = "attribute_page";
    public static final String CATEGORY_TABLE_FRAGMENT = "fragments/attributes/category_table.html :: categoryTable";
    public static final String CATEGORY_UPDATE_FRAGMENT = "fragments/attributes/category_update_form.html :: updateForm";
    public static final String VOLUME_TABLE_FRAGMENT = "fragments/attributes/volume_table.html :: volumeTable";
    public static final String VOLUME_UPDATE_FRAGMENT = "fragments/attributes/volume_update_form.html :: updateForm";
    public static final String OUTGOING_ORDER_VIEW = "outgoing_orders_page";
    public static final String OUTGOING_ORDER_FORM_FRAGMENT = "fragments/orders/outgoing_order_form.html :: orderForm";
    public static final String OUTGOING_ORDER_TABLE_FRAGMENT = "fragments/orders/order_table.html :: orderTable(page='out')";
    public static final String RECEIVING_ORDER_VIEW = "receiving_orders_page";
    public static final String RECEIVING_ORDER_FORM_FRAGMENT = "fragments/orders/receiving_order_form.html :: orderForm";
    public static final String RECEIVING_ORDER_TABLE_FRAGMENT = "fragments/orders/order_table.html :: orderTable(page='in')";
    public static final String USER_VIEW = "users_page.html";
    public static final String USER_UPDATE_FRAGMENT = "fragments/user/user_update_form.html :: updateForm";

    public static final String USER_TABLE_FRAGMENT = "fragments/user/user_table.html :: usersTable";


    private ViewNames(){}
}
