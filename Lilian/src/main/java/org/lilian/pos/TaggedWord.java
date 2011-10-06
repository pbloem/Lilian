package org.lilian.pos;

public final class TaggedWord {
	private String word;
	private Tag tag;
	
	public TaggedWord(String word, Tag tag) {
		this.word = word;
		this.tag = tag;
	}

	public String word() {
		return word;
	}

	public Tag tag() {
		return tag;
	}

	
	@Override
	public String toString() 
	{
		return word + "_" + tag;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaggedWord other = (TaggedWord) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
}
