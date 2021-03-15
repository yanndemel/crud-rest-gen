package com.octo.tools.crud.utils;

import com.octo.tools.crud.audit.SkipAuditController;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Audited
@SkipAuditController
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false, of = {"address"})
@Data
public class Mailbox {

	@Id
	private String address;
	
	private Long entityId;

}
