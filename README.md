# Note Alma Uygulaması

Bu proje, JavaFX tabanlı bir not alma uygulamasını Docker ve mikro servis mimarisi ile genişletilmiş halidir. Uygulama, modern uygulama geliştirme tekniklerini ve araçlarını kullanmaktadır.

## Özellikler

- **Mikroservis Mimarisi**: Spring Boot REST API ve JavaFX istemcisi
- **Veritabanı**: PostgreSQL ile veritabanı entegrasyonu
- **Kimlik Doğrulama**: Keycloak ile kullanıcı yönetimi ve yetkilendirme
- **Docker Entegrasyonu**: Tüm bileşenler için konteyner desteği
- **İzleme ve Loglama**: Prometheus, Grafana ve Loki ile izleme altyapısı

## Gereksinimler

- JDK 17 veya üzeri
- Maven 3.6 veya üzeri
- Docker ve Docker Compose
- Git

## Kurulum

### 1. Repoyu Klonlayın

```bash
git clone https://github.com/yourusername/noteapp.git
cd noteapp
```

### 2. Projeyi Derleyin

```bash
mvn clean package
```

### 3. Docker Compose ile Başlatın

```bash
docker-compose up -d
```

### 4. Keycloak Yapılandırması

Kurulum scriptini çalıştırın:

```bash
./scripts/setup-keycloak.sh
```

Veya manuel olarak:

1. `http://localhost:8080` adresine gidin
2. Admin paneline giriş yapın (kullanıcı adı: `admin`, şifre: `admin`)
3. "noteapp" realm'ı oluşturun
4. "noteapp-client" client'ı ekleyin
5. Kullanıcılar ve rolleri yapılandırın

### 5. JavaFX İstemci Uygulamasını Başlatın

```bash
java -jar noteapp-client/target/noteapp-client.jar
```

## Yapı

Proje aşağıdaki bileşenlerden oluşur:

- **noteapp-api**: Spring Boot REST API
- **noteapp-client**: JavaFX masaüstü uygulaması
- **PostgreSQL**: Veritabanı
- **Keycloak**: Kimlik doğrulama ve yetkilendirme sunucusu
- **Prometheus**: Metrik toplama
- **Grafana**: Görselleştirme ve dashboard'lar
- **Loki**: Log toplama ve yönetimi

## İzleme ve Dashboardlar

- Grafana: http://localhost:3000 (kullanıcı adı: `admin`, şifre: `admin`)
- Prometheus: http://localhost:9090
- Keycloak: http://localhost:8080

## Yedekleme

Veritabanını yedeklemek için backup scriptini kullanabilirsiniz:

```bash
./scripts/backup.sh
```

## Geliştirme

### API Geliştirme

Spring Boot API'yi geliştirmek için:

```bash
cd noteapp-api
mvn spring-boot:run
```

### İstemci Geliştirme

JavaFX istemcisini geliştirmek için:

```bash
cd noteapp-client
mvn javafx:run
```

## Lisans

[MIT](LICENSE)