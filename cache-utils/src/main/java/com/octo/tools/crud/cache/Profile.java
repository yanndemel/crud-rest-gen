package com.octo.tools.crud.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor
@Data
public class Profile implements Portable {

	public static final int FACTORY_ID = 1;
	public static final int CLASS_ID = 1;

	/* *
	 * Stored in cache
	 * */
	private String displayName;
	private String firstname;
	private String email;	
	private Long userId;
	// for external users
	private Long webId;
	private Long entityId;
	private Long tenantId;
	private List<Long> accountContactIds;

	/**
	 * Sent to client with profile info (not stored in cache)
	 * */
	private String token;
	private String refreshToken;
	private long expiresIn;
	
	//Set to true for internal users
	private boolean internal;
	
	public Profile(String token, String displayName, String email, Long userId, Long webId, String firstName, Long entityId, Long tenantId, boolean internal, List<Long> accountContactIds) {
		this(token, displayName, email, userId, firstName, entityId, tenantId, internal);
		this.webId = webId;
		this.accountContactIds = accountContactIds;
	}
	
	public Profile(String token, String displayName, String email, Long userId, String firstName, Long entityId, Long tenantId, boolean internal) {
		super();
		this.token = token;
		this.displayName = displayName;
		this.firstname = firstName;
		this.email = email;
		this.userId = userId;
		this.entityId = entityId;
		this.tenantId = tenantId;
		this.internal = internal;
	}

	/*Use to send RefreshToken infos*/
	public Profile(String displayName, String email, String token, String refreshToken, long expiresIn) {
		super();
		this.displayName = displayName;
		this.email = email;
		this.token = token;
		this.refreshToken = refreshToken;
		this.expiresIn = expiresIn;
		this.internal = true;
	}


	@Override
	public int getFactoryId() {
		return FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return CLASS_ID;
	}

	@Override
	public void readPortable(PortableReader portableReader) throws IOException {
		displayName = portableReader.readUTF("displayName");
		firstname = portableReader.readUTF("firstname");
		email = portableReader.readUTF("email");
		userId = portableReader.readLong("userId");
		webId = portableReader.readLong("webId");
		entityId = portableReader.readLong("entityId");
		tenantId = portableReader.readLong("tenantId");
		long[] primitiveAccountContactIds = portableReader.readLongArray("accountContactIds");
		this.accountContactIds = primitiveAccountContactIds != null ? LongStream.of(primitiveAccountContactIds).boxed().collect(Collectors.toList()) : null;
		token = portableReader.readUTF("token");
		refreshToken = portableReader.readUTF("refreshToken");
		expiresIn = portableReader.readLong("expiresIn");
		internal = portableReader.readBoolean("internal");
	}

	@Override
	public void writePortable(PortableWriter portableWriter) throws IOException {
		portableWriter.writeUTF("displayName", displayName);
		portableWriter.writeUTF("firstname", firstname);
		portableWriter.writeUTF("email", email);
		portableWriter.writeLong("userId", zeroIfNull(userId));
		portableWriter.writeLong("webId", zeroIfNull(webId));
		portableWriter.writeLong("entityId", zeroIfNull(entityId));
		portableWriter.writeLong("tenantId", zeroIfNull(tenantId));
		portableWriter.writeLongArray("accountContactIds", longArray(accountContactIds));
		portableWriter.writeUTF("token", token);
		portableWriter.writeUTF("refreshToken", refreshToken);
		portableWriter.writeLong("expiresIn", expiresIn);
		portableWriter.writeBoolean("internal", internal);
	}

	private static long zeroIfNull(Long value) {
		return Objects.isNull(value) ? 0 : value;
	}

	private static long[] longArray(List<Long> list) {
		if (list == null ||list.isEmpty()) {
			return null;
		}
		return list.stream().filter(Objects::nonNull).mapToLong(x -> x).toArray();
	}
}
