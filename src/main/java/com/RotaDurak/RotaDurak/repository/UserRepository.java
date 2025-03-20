package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    User findByEmail(String email); //Kullanıcıyı email ile bulma
    User findByPhoneNumber(String phoneNumber); //Telefon numarası ile bulma
}
