package PlainEnglish;

public class test {
	public static void main(String[] args) {
		float calls = 1;
		Search(0, 25, 10, calls);
	}
	
	static void Search(float start, float end, float value, float calls) {
		float current;
		System.out.println("Start: " + start);
		System.out.println("End: " + end);
		current = start + (end - start)/2;
		System.out.println("Current: " + current);
		if(current == value) {
			System.out.println("Number of calls: " + calls);
		}else {
			if(current < value) {
				Search(current, end, value, calls + 1);
			}else {
				Search(start, current, value, calls + 1);
			}
		}
	}
}
