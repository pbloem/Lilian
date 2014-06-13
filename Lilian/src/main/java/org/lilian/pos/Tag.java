package org.lilian.pos;

/**
 * A part of speech tag.
 * 
 * Immutable.
 * 
 * @author peter
 *
 */
public final class Tag {
	private String tag;
	private TagSet tagSet;
	
	public Tag(String tag, TagSet tagSet) {
		this.tag = tag;
		this.tagSet = tagSet;
	}

	public String getTag() {
		return tag;
	}

	public TagSet getTagSet() {
		return tagSet;
	}

	@Override
	public String toString() {
		return tag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + ((tagSet == null) ? 0 : tagSet.getClass().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (tagSet == null) {
			if (other.tagSet != null)
				return false;
		} else if (!tagSet.getClass().equals(other.tagSet.getClass()))
			return false;
		return true;
	}
	
}
