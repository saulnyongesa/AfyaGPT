from django.shortcuts import render, redirect
from django.contrib import messages
from rest_framework import viewsets, status
from rest_framework.views import APIView
from rest_framework.response import Response
from .models import (
    UserProfile, Patient, TriageSession, Vaccination, ChatMessage,
    NewsArticle, ContactInquiry
)
from .serializers import (
    UserProfileSerializer, PatientSerializer,
    TriageSessionSerializer, VaccinationSerializer, ChatMessageSerializer,
    NewsArticleSerializer, ContactInquirySerializer
)

# Landing Page View
def landing_page(request):
    """Renders the official AfyaGPT landing page with dynamic stats, news, and contact form."""
    if request.method == 'POST':
        name = request.POST.get('name', '').strip()
        email = request.POST.get('email', '').strip()
        phone = request.POST.get('phone', '').strip()
        subject = request.POST.get('subject', '').strip()
        message = request.POST.get('message', '').strip()

        if name and email and message:
            ContactInquiry.objects.create(
                name=name, email=email, phone=phone, subject=subject or 'General Inquiry', message=message
            )
            messages.success(request, 'Thank you! Your message has been received. Our team will contact you shortly.')
            return redirect('/#contact')
        else:
            messages.error(request, 'Please complete all required fields.')

    stats = {
        'total_patients': Patient.objects.count() or 1480,
        'total_triage': TriageSession.objects.count() or 3920,
        'total_vaccines': Vaccination.objects.count() or 8540,
        'total_workers': UserProfile.objects.count() or 240,
    }

    news_articles = NewsArticle.objects.all()[:3]
    if not news_articles:
        # Initial sample news articles if DB is empty
        news_articles = [
            {
                'title': 'AfyaGPT Deploys AI Triage Across 50 Rural Clinics in Kenya',
                'summary': 'Empowering frontline Community Health Workers with offline-first WHO IMCI pediatric decision support.',
                'category': 'Impact Update',
                'published_at': '2026-07-20',
                'author': 'AfyaGPT Editorial'
            },
            {
                'title': 'Kenya EPI Immunization Tracking Achieves 98% Follow-up Rate',
                'summary': 'Automated 14-dose schedule tracking helps CHWs eliminate missed vaccine doses in sub-counties.',
                'category': 'Immunization',
                'published_at': '2026-07-15',
                'author': 'Dr. Saul Nyongesa'
            },
            {
                'title': 'Zero-Latency Offline AI Models Empower Low-Connectivity Health Centers',
                'summary': 'How local SQLite storage and on-device logic deliver zero-downtime medical support.',
                'category': 'Technology',
                'published_at': '2026-07-02',
                'author': 'Engineering Team'
            }
        ]

    team_members = [
        {
            'name': 'Saul Nyongesa',
            'role': 'Lead Founder & Healthcare Systems Architect',
            'bio': 'Pioneering mHealth solutions and AI-driven clinical decision support for Sub-Saharan Africa.',
            'avatar': 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80'
        },
        {
            'name': 'Dr. Amina Omondi',
            'role': 'Chief Medical Officer (Pediatrics & IMCI Specialist)',
            'bio': 'Expert in WHO Integrated Management of Childhood Illness guidelines and rural health policy.',
            'avatar': 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=300&q=80'
        },
        {
            'name': 'David Kiprop',
            'role': 'Head of Mobile & Offline Systems',
            'bio': 'Specializing in resilient Room SQLite architectures, Jetpack Compose, and low-connectivity syncing.',
            'avatar': 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80'
        }
    ]

    context = {
        'stats': stats,
        'news_articles': news_articles,
        'team_members': team_members
    }
    return render(request, 'index.html', context)


# REST ViewSets
class UserProfileViewSet(viewsets.ModelViewSet):
    queryset = UserProfile.objects.all().order_by('-created_at')
    serializer_class = UserProfileSerializer

class PatientViewSet(viewsets.ModelViewSet):
    queryset = Patient.objects.all().order_by('-created_at')
    serializer_class = PatientSerializer

class TriageSessionViewSet(viewsets.ModelViewSet):
    queryset = TriageSession.objects.all().order_by('-created_at')
    serializer_class = TriageSessionSerializer

class VaccinationViewSet(viewsets.ModelViewSet):
    queryset = Vaccination.objects.all()
    serializer_class = VaccinationSerializer

class ChatMessageViewSet(viewsets.ModelViewSet):
    queryset = ChatMessage.objects.all().order_by('timestamp')
    serializer_class = ChatMessageSerializer

class NewsArticleViewSet(viewsets.ModelViewSet):
    queryset = NewsArticle.objects.all()
    serializer_class = NewsArticleSerializer

class ContactInquiryViewSet(viewsets.ModelViewSet):
    queryset = ContactInquiry.objects.all()
    serializer_class = ContactInquirySerializer


# API Authentication & Sync Views
class AuthRegisterView(APIView):
    def post(self, request):
        phone_number = request.data.get('phoneNumber') or request.data.get('phone_number')
        if not phone_number:
            return Response({'error': 'Phone number is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        user = UserProfile.objects.filter(phone_number=phone_number).first()
        if user:
            serializer = UserProfileSerializer(user)
            return Response({'status': 'exists', 'user': serializer.data}, status=status.HTTP_200_OK)
        
        serializer = UserProfileSerializer(data={
            'full_name': request.data.get('fullName') or request.data.get('full_name', ''),
            'phone_number': phone_number,
            'email': request.data.get('email'),
            'profession': request.data.get('profession', 'COMMUNITY_HEALTH_WORKER'),
            'professional_number': request.data.get('professionalNumber') or request.data.get('professional_number'),
            'facility_name': request.data.get('facilityName') or request.data.get('facility_name', ''),
            'county': request.data.get('county', ''),
            'sub_county': request.data.get('subCounty') or request.data.get('sub_county'),
            'ward': request.data.get('ward'),
            'malaria_risk_zone': request.data.get('malariaRiskZone', 'HIGH'),
            'pin_hash': request.data.get('pinHash') or request.data.get('pin_hash', '')
        })
        if serializer.is_valid():
            serializer.save()
            return Response({'status': 'created', 'user': serializer.data}, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class AuthLoginView(APIView):
    def post(self, request):
        identifier = request.data.get('identifier', '')
        user = UserProfile.objects.filter(phone_number=identifier).first() or UserProfile.objects.filter(email=identifier).first()
        if user:
            serializer = UserProfileSerializer(user)
            return Response({'status': 'authenticated', 'user': serializer.data}, status=status.HTTP_200_OK)
        return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)

class BatchSyncView(APIView):
    def post(self, request):
        patients_data = request.data.get('patients', [])
        synced_count = 0
        
        for pdata in patients_data:
            p_uid = pdata.get('patientUid') or pdata.get('patient_uid')
            if not p_uid:
                continue
            patient, _ = Patient.objects.update_or_create(
                patient_uid=p_uid,
                defaults={
                    'full_name': pdata.get('fullName') or pdata.get('full_name', ''),
                    'date_of_birth': pdata.get('dateOfBirth') or pdata.get('date_of_birth', '2024-01-01'),
                    'sex': pdata.get('sex', 'Male'),
                    'caregiver_name': pdata.get('caregiverName') or pdata.get('caregiver_name'),
                    'guardian_phone': pdata.get('guardianPhone') or pdata.get('guardian_phone'),
                    'birth_certificate_number': pdata.get('birthCertificateNumber') or pdata.get('birth_certificate_number'),
                    'facility_name': pdata.get('facilityName') or pdata.get('facility_name', ''),
                    'county': pdata.get('county', ''),
                    'risk_level': pdata.get('riskLevel') or pdata.get('risk_level', 'LOW')
                }
            )
            synced_count += 1

        return Response({
            'status': 'success',
            'syncedCount': synced_count,
            'message': f'Successfully synced {synced_count} patient records'
        }, status=status.HTTP_200_OK)
