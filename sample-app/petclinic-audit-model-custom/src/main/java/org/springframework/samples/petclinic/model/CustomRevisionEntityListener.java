package org.springframework.samples.petclinic.model;

import org.hibernate.envers.RevisionListener;

/**
 * Listener called by Hiibernate Envers at each new revision
 * 
 * @author OCTO
 *
 */
public class CustomRevisionEntityListener implements RevisionListener {

	/** 
	 * To be implemented : set the current user
	 * 
	 * @see org.hibernate.envers.RevisionListener#newRevision(java.lang.Object)
	 */
	@Override
	public void newRevision(Object revisionEntity) {
		CustomRevisionEntity rev = (CustomRevisionEntity)revisionEntity;
		//To replace by spring security or other call to retrieve current user
		rev.setUserName("Test user");
	}
}
