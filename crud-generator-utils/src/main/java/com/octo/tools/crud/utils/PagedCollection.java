package com.octo.tools.crud.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;


public class PagedCollection<T> implements Collection<Resource<T>> {

	private final PagedResources<Resource<T>> collection;
	private final List<Collection<Resource<T>>> otherPages;
	private final PagedCollectionLoader<T> loader;
	
	public PagedCollection(PagedResources<Resource<T>> collection, PagedCollectionLoader<T> loader) {
		super();
		this.collection = collection;
		if(collection.getMetadata().getTotalPages() > 1)
			this.otherPages = new ArrayList<>();
		else
			this.otherPages = null;
		this.loader = loader;
	}
	
	@Override
	public int size() {		
		return Math.toIntExact(collection.getMetadata().getTotalElements());
	}

	@Override
	public boolean isEmpty() {
		return collection.getMetadata().getTotalElements() == 0L;
	}

	@Override
	public boolean contains(Object o) {
		if(o == null || !(o instanceof Resource) || size() == 0)
			return false;
		return collection.getContent().contains(o) || (otherPages != null && otherPages.stream().filter(l->l.contains(o)).findFirst().isPresent());		
	}	

	@Override
	public Iterator<Resource<T>> iterator() {		
		return new PagedResourceIterator<T>(this);
	}

	@Override
	public Object[] toArray() {
		if(otherPages == null)
			return collection.getContent().toArray();
		List<Resource<T>> l = new ArrayList<>(collection.getContent());
		otherPages.forEach(list->l.addAll(list));
		return l.toArray();
	}


	@Override
	public boolean add(Resource<T> e) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean addAll(Collection<? extends Resource<T>> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public void clear() {
		throw new RuntimeException("Not Implemented");
	}

	PagedResources<Resource<T>> getCollection() {
		return collection;
	}


	Iterator<Resource<T>> loadNextPage() {
		int pageIndex =  Math.toIntExact(this.collection.getMetadata().getNumber()) + this.otherPages.size() + 1;
		Collection<Resource<T>> nextPage = loader.loadNextPage(pageIndex);
		otherPages.add(nextPage);	
		return nextPage.iterator();
	}



	@Override
	public <T> T[] toArray(T[] a) {		
		return (T[]) toArray();
	}

	boolean isLoaded(int page) {		
		return otherPages.size() >= page - collection.getMetadata().getNumber();
	}

	Iterator<Resource<T>> getPageIterator(int page) {		
		return otherPages.get(page - Math.toIntExact(collection.getMetadata().getNumber())).iterator();
	}

	

}
