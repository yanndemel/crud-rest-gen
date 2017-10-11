package com.octo.tools.crud.paging;

public interface PageLoader<T> {

	PagedCollection<T> loadNextPage() throws LoadPageException;

}
