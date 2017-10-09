package com.octo.tools.crud.utils;

import java.util.Collection;

import org.springframework.hateoas.Resource;

public interface PagedCollectionLoader<T> {

	Collection<Resource<T>> loadNextPage(int pageIndex);
}
