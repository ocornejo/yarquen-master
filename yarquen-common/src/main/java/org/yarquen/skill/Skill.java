package org.yarquen.skill;

import javax.validation.constraints.NotNull;

import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryBranchNode;

public class Skill {
	public static enum Level {
		UNKNOW(0, "Unknow"), BASIC(1, "Basic"), MEDIUM(2, "Medium"), ADVANCED(
				3, "Advanced");
		private final int id;
		private final String name;

		Level(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public static Level parse(int value) {
			for (Level l : Level.values()) {
				if (value == l.getId()) {
					return l;
				}
			}
			throw new IllegalArgumentException("Invalid level value(" + value
					+ "), it has to be in the range [0, 3]");
		}
	}

	public static final String LEVEL_SEPARATOR = ".";

	@NotNull
	private CategoryBranch categoryBranch;
	private int level;
	private double trust;
	private String color;
	//idUser for trust
	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getAsText() {
		return categoryBranch.getCode() + LEVEL_SEPARATOR + level;
	}

	public CategoryBranch getCategoryBranch() {
		return categoryBranch;
	}

	public String getCode() {
		String code = categoryBranch.getCode();
		if (level != Level.UNKNOW.getId()) {
			code += LEVEL_SEPARATOR + level;
		}
		return code;
	}
	
	public void setTrust(double trust){
		this.trust= trust;
	}
	
	public double getTrust(){
		return trust;
	}
	
	public void setColor(){
		
		if (isBetween(trust, 1, 3)) {
		  color = "bad";
		} else if (isBetween(trust, 3, 5)) {
			color = "poor";
		} else if (isBetween(trust, 5, 7)) {
			color = "regular";
		} else if (isBetween(trust, 7, 9)) {
			color = "good";
		} else if (isBetween(trust, 9, 10)) {
			color = "excellent";
		} else{
			color= "default";
		}
	}
	public String getColor(){
		return color;
	}

	/**
	 * Returns the skill's code as an array, where the first element is the
	 * argument string, the elements in the range [1, n-1] corresponds to the
	 * component code of the nodes in that position, and the last one is the
	 * level (unless is UNKNOW, case in which is omitted).
	 * 
	 * @param categoryCode
	 *            first element of the code
	 * @return skill code
	 */
	public String[] getCodeAsArray(String categoryCode) {
		int arrayLength = 1 + categoryBranch.getNodes().size();
		if (level != Level.UNKNOW.getId()) {
			arrayLength++;
		}
		final String[] codeArray = new String[arrayLength];
		int i = 0;
		codeArray[i++] = categoryCode;
		for (CategoryBranchNode node : categoryBranch.getNodes()) {
			codeArray[i++] = node.getCode();
		}
		if (level != Level.UNKNOW.getId()) {
			codeArray[i++] = String.valueOf(level);
		}
		return codeArray;
	}

	public String getLevelName() {
		return Level.parse(level).getName();
	}

	public void setCategoryBranch(CategoryBranch categoryBranch) {
		this.categoryBranch = categoryBranch;
	}

	@Override
	public String toString() {
		return "AccountSkill [categoryBranch=" + categoryBranch + ", level="
				+ level + "]";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}

		if (anObject instanceof Skill) {
			final Skill skill = (Skill) anObject;
			if (this.getCode().equals(skill.getCode())) {
				return true;
			}
		}
		return false;

	}

	public String friendlyName() {
		return categoryBranch.getNodes()
				.get(categoryBranch.getNodes().size() - 1).getName()
				+ ": " + getLevelName();
	}
	
	private boolean isBetween(double x, double lower, double upper) {
		  return lower <= x && x <= upper;
	}
}
