package account;

import javax.persistence.*;
import java.util.Set;


@Entity
@Table(name = "payments")
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "employee")
    private String employee;
    @Column(name = "period")
    private String period;
//    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name="user_id", nullable=false)
//    private User user;
    @Column(name = "salary")
    private int salary;


    public Long getId() {
        return id;
    }
//    public void setId(Long id) {
//        this.id = id;
//    }



    public Payments(String employee, String period, int salary) {
        this.employee = employee;
        this.period = period;
        this.salary = salary;
    }

    public Payments() {
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }
}
