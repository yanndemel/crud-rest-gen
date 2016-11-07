package org.springframework.samples.petclinic.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * Entity used by Hibernate Envers to handle global history versioning
 * 
 * @author OCTO
 *
 */
@Entity
@RevisionEntity( CustomRevisionEntityListener.class )
public class CustomRevisionEntity {

	@Id
	@SequenceGenerator(name = "revision_id_seq", sequenceName = "revision_id_seq", allocationSize = 1)    
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revision_id_seq")
	@Column(name = "ID", updatable = false)
	@RevisionNumber
	private Long id;

	@RevisionTimestamp
	private Long timestamp;
	
	private String userName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String username) {
		this.userName = username;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
}
