package mx.fes.unam.security;

import mx.fes.unam.model.entities.vendedor.VendedorEntity;
import mx.fes.unam.repository.VendedorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class VendedorDetailsService implements UserDetailsService {

    private final VendedorRepository vendedorRepository;

    public VendedorDetailsService(VendedorRepository vendedorRepository) {
        this.vendedorRepository = vendedorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        VendedorEntity vendedor = vendedorRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        boolean enabled = Boolean.TRUE.equals(vendedor.getActivo());

        return new User(
                vendedor.getUsername(),
                vendedor.getPassword(),
                enabled,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + vendedor.getRol().name()))
        );
    }
}
