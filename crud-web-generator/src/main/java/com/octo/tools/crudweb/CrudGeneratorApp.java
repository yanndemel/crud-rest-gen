package com.octo.tools.crudweb;

public class CrudGeneratorApp {


	public static void main(String[] args) {
		if (args == null || args.length < 2) {
			usage();
		}
		String persistenceUnitName = null;
		String destDirRelativePath = "target/admin";
		String restUrl = "@@API_URL@@";

		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(0)) {
			case '-':
				if (args[i].length() < 2)
					notValid(args, i);
				if (args[i].equals("-pu")) {
					checkArg(args, i);
					if (i < args.length - 1)
						persistenceUnitName = args[i + 1];
					else
						notValid(args, i);
				} else if (args[i].equals("-target")) {
					checkArg(args, i);
					if (i < args.length - 1)
						destDirRelativePath = args[i + 1];
					else
						notValid(args, i);
				} else if (args[i].equals("-restApiUrl")) {
					checkArg(args, i);
					if (i < args.length - 1)
						restUrl = args[i + 1];
					else
						notValid(args, i);
				}
				break;
			default:
				break;
			}
		}

		CrudGenerator generator = new CrudGenerator();
		try {			
			generator.generate(persistenceUnitName, destDirRelativePath, restUrl);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private static void notValid(String[] args, int i) {
		throw new IllegalArgumentException("Not a valid argument: " + args[i]);
	}

	private static void checkArg(String[] args, int i) {
		if (args[i].length() < 3)
			notValid(args, i);
	}

	private static void usage() {
		System.err
				.println("Usage : java -classpath crud-web-generator-<version>.jar;<path to JPA Entities jar file(s)>;"
						+ "<path to persistence.xml> com.octo.tools.crudweb.CrudGeneratorApp -pu <persistence unit name> "
						+ "[optional : -target <target directory absolute path (target/admin by default)> -restApiUrl <Rest API base URL>]");
		System.exit(-1);
	}

}
