package br.com.montreal.provapratica.ProvaPratica;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.montreal.provapratica.domain.Imagem;
import br.com.montreal.provapratica.domain.Produto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProvaPraticaApplicationTests {

	private ObjectMapper mapper = new ObjectMapper();
	
	@LocalServerPort
	private int port;
	
	@Before
	public void init() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}
	
	@Test
	public void testCreate() throws JsonProcessingException{
		Produto produto = new Produto();
		Imagem imagem = new Imagem();
		produto.setNome("Java RS");
		produto.setDescricao("Java JAX-RS com springboot");
		produto.setImagens(Arrays.asList(imagem));
		given().contentType(ContentType.JSON).body(mapper.writeValueAsString(produto)).post("/api/produto/").then().statusCode(201);
	}
	
	@Test
	public void testUpdate() throws JsonProcessingException{
		Produto produto = new Produto();
		produto.setNome("Java RS");
		produto.setDescricao("Java JAX-RS com springboot");
		given().contentType(ContentType.JSON).body(mapper.writeValueAsString(produto)).put("/api/produto/{id}", 1).then().statusCode(200);
		given().contentType(ContentType.JSON).body(mapper.writeValueAsString(produto)).put("/api/produto/{id}", 100).then().statusCode(404);
	}
	
	@Test
	public void testDelete(){
		delete("/api/produto/{id}", 11).then().statusCode(200);
		delete("/api/produto/{id}", 12).then().statusCode(404);
	}

	@Test
	public void testStatusCodeOk() {
		get("/api/produto/").then().statusCode(200).and().contentType(ContentType.JSON).and().body("nome", not(empty()));
		get("/api/produto/imagem/").then().statusCode(200).and().contentType(ContentType.JSON);
		get("/api/produto/1").then().statusCode(200).and().contentType(ContentType.JSON);
		get("/api/produto/imagem/1").then().statusCode(200).and().contentType(ContentType.JSON);
		get("/api/produto/getAllByProdutoPai/1").then().statusCode(200).and().contentType(ContentType.JSON);
		get("/api/produto/imagens/1").then().statusCode(200).and().contentType(ContentType.JSON);

	}
	
	@Test
	public void testStatusCodeNotFound() {
		get("/api/produto/100").then().statusCode(404).and().contentType(ContentType.JSON);
		get("/api/produto/imagem/100").then().statusCode(404).and().contentType(ContentType.JSON);

	}
	
	/**
	 * 1. Test Recuperar todos os Produtos excluindo os relacionamentos;
	 */
	@Test
	public void testCheckAllProdutosNoRelationship() {
		get("/api/produto/").then().body(not(hasKey("imagens")));
		get("/api/produto/").then().body(not(hasKey("produtoPai")));
	}
	
	/**
	 * 2. Recuperar todos os Produtos incluindo um relacionamento específico Imagem;
	 */
	@Test
	public void testCheckAllProdutosWithRelationship() {
		get("/api/produto/imagem/").then().body("nome", notNullValue()).and().body("id", notNullValue()).and().body("imagens", notNullValue());
	}
	
	
	/**
	 * 3. Igual ao no 1 utilizando um id de produto específico;
	 */
	@Test
	public void testCheckByIdProdutoNoRelationship() {
		get("/api/produto/1").then().body(not(hasKey("imagens")));
	}
	
	/**
	 * 4. Igual ao no 2 utilizando um id de produto específico;
	 */
	@Test
	public void testCheckByIdProdutoWithRelationship() {
		get("/api/produto/imagem/1").then().body("nome", notNullValue()).and().body("id", notNullValue());
		List<Imagem> imagens = get("/api/produto/imagem/1").getBody().jsonPath().getList("imagens", Imagem.class);
		assertThat(imagens, everyItem(hasProperty("id", notNullValue())));
	    assertThat(imagens, everyItem(hasProperty("tipoImagem", notNullValue())));
	}
	
	/**
	 * 5. Igual ao no 2 utilizando um id de produto específico;
	 */
	@Test
	public void testCheckByIdProdutoPai() {
		List<Produto> produtos = get("api/produto/getAllByProdutoPai/1").getBody().jsonPath().getList("produtos", Produto.class);
		assertThat(produtos, everyItem(hasProperty("id", notNullValue())));
	    assertThat(produtos, everyItem(hasProperty("nome", notNullValue())));
	}
	
	/**
	 * 6. Recupera a coleção de Imagens para um id de produto específico;
	 */
	@Test
	public void testCheckImagensByIdProduto() {
		List<Imagem> imagens = Arrays.asList(given().when().get("/api/produto/imagens/1").as(Imagem[].class));
		assertThat(imagens, everyItem(hasProperty("id", notNullValue())));
	    assertThat(imagens, everyItem(hasProperty("tipoImagem", notNullValue())));
	}
	

}
