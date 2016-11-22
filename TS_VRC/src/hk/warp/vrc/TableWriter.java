package hk.warp.vrc;

public interface TableWriter {
	
	void rowStart();
	void rowEnd();
	void writeCell(String aContent);
	void tableStart();
	void tableEnd();

	public static class HTMLTableWriter implements TableWriter
	{

		@Override
		public void writeCell(String aContent) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void rowStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void rowEnd() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void tableStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void tableEnd() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
