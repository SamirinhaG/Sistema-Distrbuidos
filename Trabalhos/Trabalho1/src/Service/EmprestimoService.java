package Service;

import Entities.Emprestimo;

import java.util.ArrayList;
import java.util.List;

public class EmprestimoService {

    private EmprestimoService emprestimo;
    private List<Emprestimo> emprestimos =  new ArrayList<>();

    public EmprestimoService(EmprestimoService emprestimo) {
        this.emprestimo = emprestimo;
    }

    public void criarEmprestimo() {

    }

    public List<Emprestimo> listarEmprestimos() {
        return emprestimos;
    }

    public EmprestimoService getEmprestimo() {
        return emprestimo;
    }


}
