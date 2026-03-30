package com.github.felipex.votecoop.service;

import com.github.felipex.votecoop.domain.Pauta;
import com.github.felipex.votecoop.dto.request.CadastrarPautaRequest;
import com.github.felipex.votecoop.exception.AppException;
import com.github.felipex.votecoop.repository.PautaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PautaService {

    private final PautaRepository pautaRepository;

    @Transactional
    public Pauta cadastrar(CadastrarPautaRequest request) {
        Pauta pauta = new Pauta(request.titulo(), request.descricao());
        return pautaRepository.save(pauta);
    }

    @Transactional(readOnly = true)
    public List<Pauta> listarTodas() {
        return pautaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pauta buscarPorId(Long id) {
        return pautaRepository.findById(id)
                .orElseThrow(() -> new AppException(AppException.Tipo.RECURSO_NAO_ENCONTRADO, id));
    }
}
