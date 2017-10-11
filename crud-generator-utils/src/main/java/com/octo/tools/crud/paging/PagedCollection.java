package com.octo.tools.crud.paging;

import java.util.Iterator;

public class PagedCollection<T> implements Iterable<T> {

	private Iterable<T> currentPage;
	
	private boolean hasNextPage;
	
	private PageLoader<T> pageLoader;

	public PagedCollection(Iterable<T> currentPage, boolean hasNextPage, PageLoader<T> loader) {
		this.currentPage = currentPage;
		this.hasNextPage = hasNextPage;
		this.pageLoader = loader;
	}
	
	private void getNextPage() throws LoadPageException {
		PagedCollection<T> nextPage = pageLoader.loadNextPage();
		currentPage = nextPage.currentPage;
		hasNextPage = nextPage.hasNextPage;
		pageLoader = nextPage.pageLoader;
	}

	public Iterable<T> getCurrentPage() {
		return currentPage;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			private Iterator<T> it = currentPage.iterator();
			
			@Override
			public boolean hasNext() {
				boolean hasNext = it.hasNext();
				if(!hasNext && hasNextPage) {
					try {
						getNextPage();
					} catch (LoadPageException e) {
						return false;
					}
					if(currentPage == null)
						return false;
					it = currentPage.iterator();
					hasNext = it.hasNext();
				}
				return hasNext;
			}

			@Override
			public T next() {
				return it.next();
			}
			
		};
	}
	
	
	
}
