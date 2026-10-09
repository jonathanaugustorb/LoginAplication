package com.example.LoginCompleto.item.service;

import com.example.LoginCompleto.common.exception.BusinessException;
import com.example.LoginCompleto.common.service.AbstractCrudService;
import com.example.LoginCompleto.infra.security.Permissions;
import com.example.LoginCompleto.item.dto.ItemDTO;
import com.example.LoginCompleto.item.dto.ItemFilterDTO;
import com.example.LoginCompleto.item.entity.Item;
import com.example.LoginCompleto.item.mapper.ItemMapper;
import com.example.LoginCompleto.item.repository.IItemRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ItemService extends AbstractCrudService<Item, IItemRepository> {

    private static final String DUPLICATED_CODE_MESSAGE = "Já existe um item com este código";
    private static final String CONCURRENT_UPDATE_MESSAGE =
            "Este item foi alterado por outro usuário. Recarregue a página e tente novamente.";

    private final ItemMapper itemMapper;
    private final AuditorAware<String> auditorAware;

    public ItemService(IItemRepository itemRepository, ItemMapper itemMapper, AuditorAware<String> auditorAware) {
        super(itemRepository, "Item");
        this.itemMapper = itemMapper;
        this.auditorAware = auditorAware;
    }

    public Page<Item> search(ItemFilterDTO filter, Pageable pageable) {
        return repository.search(filter, pageable);
    }

    public long countLowStock() {
        return repository.countLowStock();
    }

    @PreAuthorize(Permissions.ESTOQUE)
    public Item create(ItemDTO dto) {
        Item item = itemMapper.toEntity(dto);

        if (repository.existsByCode(item.getCode())) {
            throw new BusinessException(DUPLICATED_CODE_MESSAGE);
        }

        return saveUnique(item);
    }

    @PreAuthorize(Permissions.SUPERVISOR)
    public Item update(String id, ItemDTO dto) {
        Item item = findById(id);

        if (!Objects.equals(item.getVersion(), dto.getVersion())) {
            throw new BusinessException(CONCURRENT_UPDATE_MESSAGE);
        }
        if (repository.existsByCodeAndIdNot(itemMapper.normalizeCode(dto.getCode()), id)) {
            throw new BusinessException(DUPLICATED_CODE_MESSAGE);
        }

        itemMapper.updateEntity(item, dto);
        return saveUnique(item);
    }

    @PreAuthorize(Permissions.ESTOQUE)
    public void addStock(String id, int quantity) {
        findById(id);
        String updatedBy = auditorAware.getCurrentAuditor().orElse(null);

        if (!repository.addQuantity(id, quantity, ItemDTO.MAX_QUANTITY, updatedBy)) {
            throw new BusinessException("A quantidade em estoque ultrapassaria o limite de 1.000.000 unidades");
        }
    }

    @Override
    @PreAuthorize(Permissions.GERENTE)
    public void deleteById(String id) {
        super.deleteById(id);
    }

    private Item saveUnique(Item item) {
        try {
            return repository.save(item);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(DUPLICATED_CODE_MESSAGE);
        } catch (OptimisticLockingFailureException exception) {
            throw new BusinessException(CONCURRENT_UPDATE_MESSAGE);
        }
    }
}
