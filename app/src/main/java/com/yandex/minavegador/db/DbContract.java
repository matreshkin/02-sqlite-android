package com.yandex.minavegador.db;

/**
 * Created by user on 31/07/2016.
 */
interface DbContract {
    String DB_NAME = "main.sqlite";

    String HISTORY = "history";
    interface History {
        String ID = "rowid";
        String PAGE_ID = "history_page_id";
        String LAST_VISITED = "history_last_visited";
        String IDX_LAST_VISITED = "idx_history_last_visited";
    }

    String PAGES = "pages";
    interface Pages {
        String PAGE_ID = "page_id";
        String PAGE_URL = "page_url";
        String FAVICON_ID = "page_favicon_id";
    }

    String FAVICONS = "favicons";
    interface Favicons {
        String ICON_ID = "favicon_id";
        String ICON_URL = "favicon_icon_url";
        String LAST_UPDATED = "favicon_last_updated";
    }
}
