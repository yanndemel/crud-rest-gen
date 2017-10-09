package com.octo.tools.crud.utils;

import java.util.Iterator;

import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.hateoas.Resource;

public class PagedResourceIterator<T> implements Iterator<Resource<T>>{

	private final PagedCollection<T> pagedCollection;
	private final int startIndex;
	private int index;
	private int page;
	private Iterator<Resource<T>> iterator;
	
	public PagedResourceIterator(PagedCollection<T> pagedCollection) {
		this.pagedCollection = pagedCollection;
		iterator = this.pagedCollection.getCollection().iterator();
		PageMetadata metadata = pagedCollection.getCollection().getMetadata();
		this.startIndex = Math.toIntExact(metadata.getNumber() * metadata.getSize());
		this.page = Math.toIntExact(metadata.getNumber());
		index = -1;
	}

	@Override
	public boolean hasNext() {
		boolean hasNext = iterator.hasNext();
		if(!hasNext && this.pagedCollection.size() - 1 > index + startIndex) {
			if(pagedCollection.isLoaded(page + 1))
				iterator = pagedCollection.getPageIterator(page + 1);
			else
				iterator = pagedCollection.loadNextPage();
			page++;
			hasNext = iterator.hasNext();
		}
		return hasNext;
	}

	@Override
	public Resource<T> next() {
		index++;
		return iterator.next();
	}

}
