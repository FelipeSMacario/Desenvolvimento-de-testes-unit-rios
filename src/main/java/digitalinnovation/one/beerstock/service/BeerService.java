package digitalinnovation.one.beerstock.service;

import digitalinnovation.one.beerstock.dto.BeerDTO;
import digitalinnovation.one.beerstock.entity.Beer;
import digitalinnovation.one.beerstock.exception.BeerAlreadyRegisteredException;
import digitalinnovation.one.beerstock.exception.BeerMinimumStockException;
import digitalinnovation.one.beerstock.exception.BeerNotFoundException;
import digitalinnovation.one.beerstock.exception.BeerStockExceededException;
import digitalinnovation.one.beerstock.mapper.BeerMapper;
import digitalinnovation.one.beerstock.repository.BeerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerService {

    private final BeerRepository beerRepository;

    private final BeerMapper beerMapper = BeerMapper.INSTANCE;

    public BeerDTO createBeer(BeerDTO beerDTO) throws BeerAlreadyRegisteredException {
        verifyIfIsAlreadyRegistered(beerDTO.getName());
        Beer beer = beerMapper.toModel(beerDTO);
        Beer savedBeer = beerRepository.save(beer);
        return beerMapper.toDTO(savedBeer);
    }

    public BeerDTO findByName(String name) throws BeerNotFoundException {
        Beer foundBeer = beerRepository.findByName(name)
                .orElseThrow(() -> new BeerNotFoundException(name));
        return beerMapper.toDTO(foundBeer);
    }

    public List<BeerDTO> listAll() {
        return beerRepository.findAll()
                .stream()
                .map(beerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteByID(Long id) throws BeerNotFoundException {
        verifyIfExists(id);
        beerRepository.deleteById(id);
    }

    public void verifyIfIsAlreadyRegistered(String name) throws BeerAlreadyRegisteredException {
        Optional<Beer> optSavedBeer = beerRepository.findByName(name);
        if (optSavedBeer.isPresent()) {
            throw new BeerAlreadyRegisteredException(name);
        }
    }

    private Beer verifyIfExists(Long id) throws BeerNotFoundException {
        return beerRepository.findById(id)
                .orElseThrow(() -> new BeerNotFoundException(id));
    }

    public BeerDTO increment(Long id, int quantityToIncrement) throws BeerNotFoundException, BeerStockExceededException {
        Beer beerToIncrementStock = verifyIfExists(id);
        int quantityAfterIncrement = quantityToIncrement + beerToIncrementStock.getQuantity();
        if (quantityAfterIncrement <= beerToIncrementStock.getMax()) {
            beerToIncrementStock.setQuantity(beerToIncrementStock.getQuantity() + quantityToIncrement);
            Beer incrementedBeerStock = beerRepository.save(beerToIncrementStock);
            return beerMapper.toDTO(incrementedBeerStock);
        }
        throw new BeerStockExceededException(id, quantityToIncrement);

    }

    public BeerDTO decrement(Long id, int quantityToDecrement) throws BeerNotFoundException, BeerMinimumStockException {
        Beer beerToDecrementStock = verifyIfExists(id);
        int quantityAfterDecrement = beerToDecrementStock.getQuantity() - quantityToDecrement;
        if (quantityAfterDecrement >= 0) {
            beerToDecrementStock.setQuantity(beerToDecrementStock.getQuantity() - quantityToDecrement);
            Beer incrementedBeerStock = beerRepository.save(beerToDecrementStock);
            return beerMapper.toDTO(incrementedBeerStock);
        }
        throw new BeerMinimumStockException(id, quantityToDecrement);

    }
}
