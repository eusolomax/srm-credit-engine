package com.srm.credit_engine.service;

import java.util.Optional;

import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.repository.ReceivableRepository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceivableServiceTest {

    private final ReceivableRepository repository = mock(ReceivableRepository.class);
    private final ReceivableService service = new ReceivableService(repository);

    // Garante que um novo recebível seja encaminhado ao repositório e retornado.
    @Test
    void shouldSaveReceivable() {
        Receivable receivable = mock(Receivable.class);
        when(repository.save(receivable)).thenReturn(receivable);

        Receivable result = service.save(receivable);

        assertThat(result).isSameAs(receivable);
        verify(repository).save(receivable);
    }

    // Garante que o serviço devolva o recebível encontrado pelo identificador.
    @Test
    void shouldFindExistingReceivable() {
        Long id = 1L;
        Receivable receivable = mock(Receivable.class);

        when(repository.findById(id)).thenReturn(Optional.of(receivable));

        Optional<Receivable> result = service.findById(id);

        assertThat(result).containsSame(receivable);
        verify(repository).findById(id);
    }

    // Garante que a busca retorne vazio quando o recebível não existir.
    @Test
    void shouldReturnEmptyWhenReceivableDoesNotExist() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Receivable> result = service.findById(id);

        assertThat(result).isEmpty();
        verify(repository).findById(id);
    }
}
