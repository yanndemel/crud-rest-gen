package com.octo.tools.crud.web;

import java.io.Serializable;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class MediaType extends MimeType implements Serializable {

	private static final long serialVersionUID = 2069937152339670231L;

	/**
	 * Public constant media type that includes all media ranges (i.e. "&#42;/&#42;").
	 */
	public static final MediaType ALL;

	/**
	 * A String equivalent of {@link MediaType#ALL}.
	 */
	public static final String ALL_VALUE = "*/*";

	/**
	 *  Public constant media type for {@code application/atom+xml}.
	 */
	public static final MediaType APPLICATION_ATOM_XML;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_ATOM_XML}.
	 */
	public static final String APPLICATION_ATOM_XML_VALUE = "application/atom+xml";

	/**
	 * Public constant media type for {@code application/cbor}.
	 * @since 5.2
	 */
	public static final MediaType APPLICATION_CBOR;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_CBOR}.
	 * @since 5.2
	 */
	public static final String APPLICATION_CBOR_VALUE = "application/cbor";

	/**
	 * Public constant media type for {@code application/x-www-form-urlencoded}.
	 */
	public static final MediaType APPLICATION_FORM_URLENCODED;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_FORM_URLENCODED}.
	 */
	public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";

	/**
	 * Public constant media type for {@code application/json}.
	 */
	public static final MediaType APPLICATION_JSON;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_JSON}.
	 * @see #APPLICATION_JSON_UTF8_VALUE
	 */
	public static final String APPLICATION_JSON_VALUE = "application/json";

	/**
	 * Public constant media type for {@code application/octet-stream}.
	 */
	public static final MediaType APPLICATION_OCTET_STREAM;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_OCTET_STREAM}.
	 */
	public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

	/**
	 * Public constant media type for {@code application/pdf}.
	 * @since 4.3
	 */
	public static final MediaType APPLICATION_PDF;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_PDF}.
	 * @since 4.3
	 */
	public static final String APPLICATION_PDF_VALUE = "application/pdf";

	/**
	 * Public constant media type for {@code application/problem+json}.
	 * @since 5.0
	 * @see <a href="https://tools.ietf.org/html/rfc7807#section-6.1">
	 *     Problem Details for HTTP APIs, 6.1. application/problem+json</a>
	 */
	public static final MediaType APPLICATION_PROBLEM_JSON;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_PROBLEM_JSON}.
	 * @since 5.0
	 */
	public static final String APPLICATION_PROBLEM_JSON_VALUE = "application/problem+json";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_PROBLEM_JSON_UTF8}.
	 * @since 5.0
	 * @deprecated as of 5.2 in favor of {@link #APPLICATION_PROBLEM_JSON_VALUE}
	 * since major browsers like Chrome
	 * <a href="https://bugs.chromium.org/p/chromium/issues/detail?id=438464">
	 * now comply with the specification</a> and interpret correctly UTF-8 special
	 * characters without requiring a {@code charset=UTF-8} parameter.
	 */
	@Deprecated
	public static final String APPLICATION_PROBLEM_JSON_UTF8_VALUE = "application/problem+json;charset=UTF-8";

	/**
	 * Public constant media type for {@code application/problem+xml}.
	 * @since 5.0
	 * @see <a href="https://tools.ietf.org/html/rfc7807#section-6.2">
	 *     Problem Details for HTTP APIs, 6.2. application/problem+xml</a>
	 */
	public static final MediaType APPLICATION_PROBLEM_XML;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_PROBLEM_XML}.
	 * @since 5.0
	 */
	public static final String APPLICATION_PROBLEM_XML_VALUE = "application/problem+xml";

	/**
	 * Public constant media type for {@code application/rss+xml}.
	 * @since 4.3.6
	 */
	public static final MediaType APPLICATION_RSS_XML;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_RSS_XML}.
	 * @since 4.3.6
	 */
	public static final String APPLICATION_RSS_XML_VALUE = "application/rss+xml";

	/**
	 * Public constant media type for {@code application/stream+json}.
	 * @since 5.0
	 */
	public static final MediaType APPLICATION_STREAM_JSON;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_STREAM_JSON}.
	 * @since 5.0
	 */
	public static final String APPLICATION_STREAM_JSON_VALUE = "application/stream+json";

	/**
	 * Public constant media type for {@code application/xhtml+xml}.
	 */
	public static final MediaType APPLICATION_XHTML_XML;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_XHTML_XML}.
	 */
	public static final String APPLICATION_XHTML_XML_VALUE = "application/xhtml+xml";

	/**
	 * Public constant media type for {@code application/xml}.
	 */
	public static final MediaType APPLICATION_XML;

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_XML}.
	 */
	public static final String APPLICATION_XML_VALUE = "application/xml";

	/**
	 * Public constant media type for {@code image/gif}.
	 */
	public static final MediaType IMAGE_GIF;

	/**
	 * A String equivalent of {@link MediaType#IMAGE_GIF}.
	 */
	public static final String IMAGE_GIF_VALUE = "image/gif";

	/**
	 * Public constant media type for {@code image/jpeg}.
	 */
	public static final MediaType IMAGE_JPEG;

	/**
	 * A String equivalent of {@link MediaType#IMAGE_JPEG}.
	 */
	public static final String IMAGE_JPEG_VALUE = "image/jpeg";

	/**
	 * Public constant media type for {@code image/png}.
	 */
	public static final MediaType IMAGE_PNG;

	/**
	 * A String equivalent of {@link MediaType#IMAGE_PNG}.
	 */
	public static final String IMAGE_PNG_VALUE = "image/png";

	/**
	 * Public constant media type for {@code multipart/form-data}.
	 */
	public static final MediaType MULTIPART_FORM_DATA;

	/**
	 * A String equivalent of {@link MediaType#MULTIPART_FORM_DATA}.
	 */
	public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

	/**
	 * Public constant media type for {@code multipart/mixed}.
	 * @since 5.2
	 */
	public static final MediaType MULTIPART_MIXED;

	/**
	 * A String equivalent of {@link MediaType#MULTIPART_MIXED}.
	 * @since 5.2
	 */
	public static final String MULTIPART_MIXED_VALUE = "multipart/mixed";

	/**
	 * Public constant media type for {@code text/event-stream}.
	 * @since 4.3.6
	 * @see <a href="https://www.w3.org/TR/eventsource/">Server-Sent Events W3C recommendation</a>
	 */
	public static final MediaType TEXT_EVENT_STREAM;

	/**
	 * A String equivalent of {@link MediaType#TEXT_EVENT_STREAM}.
	 * @since 4.3.6
	 */
	public static final String TEXT_EVENT_STREAM_VALUE = "text/event-stream";

	/**
	 * Public constant media type for {@code text/html}.
	 */
	public static final MediaType TEXT_HTML;

	/**
	 * A String equivalent of {@link MediaType#TEXT_HTML}.
	 */
	public static final String TEXT_HTML_VALUE = "text/html";

	/**
	 * Public constant media type for {@code text/markdown}.
	 * @since 4.3
	 */
	public static final MediaType TEXT_MARKDOWN;

	/**
	 * A String equivalent of {@link MediaType#TEXT_MARKDOWN}.
	 * @since 4.3
	 */
	public static final String TEXT_MARKDOWN_VALUE = "text/markdown";

	/**
	 * Public constant media type for {@code text/plain}.
	 */
	public static final MediaType TEXT_PLAIN;

	/**
	 * A String equivalent of {@link MediaType#TEXT_PLAIN}.
	 */
	public static final String TEXT_PLAIN_VALUE = "text/plain";

	/**
	 * Public constant media type for {@code text/xml}.
	 */
	public static final MediaType TEXT_XML;

	/**
	 * A String equivalent of {@link MediaType#TEXT_XML}.
	 */
	public static final String TEXT_XML_VALUE = "text/xml";

	static {
		try {
			// Not using "valueOf' to avoid static init cost
			ALL = new MediaType("*", "*");
			APPLICATION_ATOM_XML = new MediaType("application", "atom+xml");
			APPLICATION_CBOR = new MediaType("application", "cbor");
			APPLICATION_FORM_URLENCODED = new MediaType("application", "x-www-form-urlencoded");
			APPLICATION_JSON = new MediaType("application", "json");
			APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");
			APPLICATION_PDF = new MediaType("application", "pdf");
			APPLICATION_PROBLEM_JSON = new MediaType("application", "problem+json");
			APPLICATION_PROBLEM_XML = new MediaType("application", "problem+xml");
			APPLICATION_RSS_XML = new MediaType("application", "rss+xml");
			APPLICATION_STREAM_JSON = new MediaType("application", "stream+json");
			APPLICATION_XHTML_XML = new MediaType("application", "xhtml+xml");
			APPLICATION_XML = new MediaType("application", "xml");
			IMAGE_GIF = new MediaType("image", "gif");
			IMAGE_JPEG = new MediaType("image", "jpeg");
			IMAGE_PNG = new MediaType("image", "png");
			MULTIPART_FORM_DATA = new MediaType("multipart", "form-data");
			MULTIPART_MIXED = new MediaType("multipart", "mixed");
			TEXT_EVENT_STREAM = new MediaType("text", "event-stream");
			TEXT_HTML = new MediaType("text", "html");
			TEXT_MARKDOWN = new MediaType("text", "markdown");
			TEXT_PLAIN = new MediaType("text", "plain");
			TEXT_XML = new MediaType("text", "xml");
		} catch (MimeTypeParseException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Create a new {@code MediaType} for the given primary type.
	 * <p>The {@linkplain #getSubtype() subtype} is set to "&#42;", parameters empty.
	 * @param type the primary type
	 * @throws MimeTypeParseException 
	 * @throws IllegalArgumentException if any of the parameters contain illegal characters
	 */
	public MediaType(String type) throws MimeTypeParseException {
		super(type);
	}

	/**
	 * Create a new {@code MediaType} for the given primary type and subtype.
	 * <p>The parameters are empty.
	 * @param type the primary type
	 * @param subtype the subtype
	 * @throws MimeTypeParseException 
	 * @throws IllegalArgumentException if any of the parameters contain illegal characters
	 */
	public MediaType(String type, String subtype) throws MimeTypeParseException {
		super(type, subtype);
	}


}
