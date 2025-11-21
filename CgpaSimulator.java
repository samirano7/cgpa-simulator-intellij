import java.util.Scanner;
public class CgpaSimulator {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        //  Ask for last semester CGPA and units
        System.out.print("Enter your last semester CGPA (0 if first semester): ");
        double lastCgpa = input.nextDouble();

        System.out.print("Enter your last semester total credit units (0 if first semester): ");
        int lastCreditUnits = input.nextInt();

        //  Ask how many courses for this semester
        System.out.print("How many courses are you offering this semester? ");
        int numberOfCourses = input.nextInt();
        input.nextLine(); // clear buffer

        double termGradePointsSum = 0;
        int termUnitsSum = 0;

        System.out.println("\n-- Enter Your Courses --");

        //  Loop through courses
        for (int i = 1;
             i <= numberOfCourses; i++) {

            System.out.println("\nCourse " + i + ":");

            System.out.print("Course Code: ");
            String courseCode = input.nextLine();

            System.out.print("Course Unit: ");
            int courseUnit = input.nextInt();

            System.out.print("Grade (1 - 5): ");
            double grade = input.nextDouble();
            input.nextLine(); // clear buffer

           //for typo error and error handling
            if (grade < 1 || grade > 5) {
                System.out.println("Invalid grade! Enter value between (1 - 5.)");
                i--;
                continue;
            }

            if (courseUnit <= 0) {
                System.out.println("Course unit must be greater than 0!");
                i--;
                continue;
            }

            // accumulate totals
            termUnitsSum += courseUnit;
            termGradePointsSum += (grade * courseUnit);
        }

        //  Calculate CGPA
        double lastGradePoints = lastCgpa * lastCreditUnits;

        double grandTotalGradePoints = lastGradePoints + termGradePointsSum;
        int grandTotalUnits = lastCreditUnits + termUnitsSum;

        if (grandTotalUnits == 0) {
            System.out.println("Error: total units cannot be zero.");
            return;
        }

        double newCgpa = grandTotalGradePoints / grandTotalUnits;


        // Print results
        System.out.println("\n======== CGPA RESULT ========");
        System.out.println("Last Semester CGPA: " + lastCgpa);
        System.out.println("Last Semester Units: " + lastCreditUnits);
        System.out.println("This Semester Units: " + termUnitsSum);
        System.out.println("Total Units: " + grandTotalUnits);
        System.out.println("Your New CGPA is: " + String.format("%.2f", newCgpa));
        System.out.println("=============================\n");

        //cgpa class
        if (newCgpa>=4.5){
            System.out.println("You are on first class! ");
        } else if (newCgpa<4.5 && newCgpa>=3.5) {
            System.out.println("You are on second class Upper! ");
        } else if (newCgpa <3.5 && newCgpa>=3.0) {
            System.out.println("You are on Second class Lower! ");
        } else if (newCgpa<3.0 && newCgpa>=2.0) {
            System.out.println("You are on Third class Upper!");
        }
        else{
            System.out.println("Advice to Withdraw!");}
        input.close();

    }
}
