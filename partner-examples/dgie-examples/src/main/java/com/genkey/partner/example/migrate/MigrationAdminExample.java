package com.genkey.partner.example.migrate;

import com.genkey.partner.example.PartnerExample;
import com.genkey.abisclient.service.RestServices;
import com.genkey.partner.dgie.migrate.LegacyMigrationService;

public class MigrationAdminExample extends PartnerExample{

	LegacyMigrationService migrationService =null;
	//
	
	public LegacyMigrationService getMigrationService() {
		if (migrationService == null) {
			RestServices.getInstance().registerServiceClass(LegacyMigrationService.class, 
									PartnerExample.getPrimaryHost(), PartnerExample.getServicePort()); 
			
			migrationService = RestServices.getInstance().accessServiceInstance(LegacyMigrationService.class);
		}
		return migrationService;
	}
	
	public static void main(String [] args) {
		MigrationAdminExample example = new MigrationAdminExample();
		example.processCommandLine(args);
	}
	
	public void setUp() {
		super.setUp();
		LegacyMigrationService service = getMigrationService();
		boolean available = service.testAvailable();
		assertTrue(available);
		
	}
	
	
	@Override
	protected void runAllExamples() {
//		cleanTestData();
//		testBatch();
		testSingleBatch();
//		testStop();
//		runStandardMigration();
//		resetStatusTable();
//		testMisc();
	}

	public void resetStatusTable() {
		LegacyMigrationService service = getMigrationService();
		service.setProductionMode(true);
		service.resetStatusTable();
	}
	
	public void testSingleBatch() {
		LegacyMigrationService service = getMigrationService();
		service.setBatchSize(100);
		service.setConcurrency(1);
		service.executeSingleBatch();		
	}
	
	public void testMisc() {
		LegacyMigrationService service = getMigrationService();
		service.setBatchSize(13);
		service.setConcurrency(1);
		service.executeSingleBatch();

		//	service.resetStatusTable();
		/*

		service.resetStatusTable();
		service.setConcurrency(2);
		service.resume();
		*/
		/*
		service.setBatchSize(37);
		service.setTargetValue(9);
		service.executeSingleBatch();
		*/
		
	}
	
	
	public void testStop() {
		LegacyMigrationService service = getMigrationService();
		service.pause();
		
	}

	public void cleanTestData() {
		LegacyMigrationService service = getMigrationService();
		service.resetStatusTable();
	}
	
	public void testBatch() {
		LegacyMigrationService service = getMigrationService();
		service.executeSingleBatch();
	}
	
	public void runStandardMigration() {
		LegacyMigrationService service = getMigrationService();
		service.setConcurrency(8);
		service.start();
	}
	
}
