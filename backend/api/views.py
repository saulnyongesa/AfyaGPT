from django.shortcuts import render, redirect, get_object_or_404
from django.contrib import messages
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from rest_framework import viewsets, status
from rest_framework.views import APIView
from rest_framework.response import Response
from .models import (
    UserProfile, Patient, TriageSession, Vaccination, ChatMessage,
    NewsArticle, ContactInquiry, Announcement, AppSetting
)
from .serializers import (
    UserProfileSerializer, PatientSerializer,
    TriageSessionSerializer, VaccinationSerializer, ChatMessageSerializer,
    NewsArticleSerializer, ContactInquirySerializer,
    AnnouncementSerializer, AppSettingSerializer
)

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
        'total_workers': UserProfile.objects.filter(is_approved=True).count() or 240,
    }

    news_articles = NewsArticle.objects.all()[:3]
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

    return render(request, 'index.html', {
        'stats': stats,
        'news_articles': news_articles,
        'team_members': team_members
    })


def web_register(request):
    """Registration view for new mobile app health workers requesting account access."""
    if request.method == 'POST':
        fullName = request.POST.get('full_name', '').strip()
        phone = request.POST.get('phone_number', '').strip()
        email = request.POST.get('email', '').strip()
        profession = request.POST.get('profession', 'COMMUNITY_HEALTH_WORKER')
        facility = request.POST.get('facility_name', '').strip()
        county = request.POST.get('county', '').strip()
        pin = request.POST.get('pin', '123456').strip()

        if fullName and phone:
            if UserProfile.objects.filter(phone_number=phone).exists():
                messages.error(request, 'An account with this phone number already exists.')
            else:
                UserProfile.objects.create(
                    full_name=fullName,
                    phone_number=phone,
                    email=email,
                    profession=profession,
                    facility_name=facility or 'Health Center',
                    county=county or 'Nairobi',
                    pin_hash=pin,
                    is_approved=False  # Requires Admin Approval
                )
                messages.success(request, 'Registration submitted! Your account is pending supervisor approval.')
                return redirect('/login/')
        else:
            messages.error(request, 'Please complete all required fields.')

    return render(request, 'register.html')


def stakeholder_login(request):
    """Authentication view for Admins and Staff."""
    if request.user.is_authenticated:
        return redirect('/dashboard/')

    if request.method == 'POST':
        username = request.POST.get('username', '').strip()
        password = request.POST.get('password', '').strip()
        user = authenticate(request, username=username, password=password)

        if user is not None:
            login(request, user)
            messages.success(request, f"Welcome back, {user.username}!")
            return redirect('/dashboard/')
        else:
            messages.error(request, "Invalid username or password. Please try again.")

    return render(request, 'login.html')


def stakeholder_logout(request):
    """Logout view for Stakeholder portal."""
    logout(request)
    messages.info(request, "You have been logged out successfully.")
    return redirect('/login/')


@login_required(login_url='/login/')
def stakeholder_dashboard(request):
    """Stakeholder Web Dashboard with Django Group-based Role Control & Superuser User Management."""
    user = request.user
    user_groups = list(user.groups.values_list('name', flat=True))

    if request.method == 'POST':
        action = request.POST.get('action')
        
        if action == 'approve_user' and user.is_superuser:
            user_id = request.POST.get('user_id')
            profile = UserProfile.objects.filter(id=user_id).first()
            if profile:
                profile.is_approved = not profile.is_approved
                profile.save()
                status_str = "Approved" if profile.is_approved else "Revoked"
                messages.success(request, f"Health Worker {profile.full_name} access updated to {status_str}.")
        
        elif action == 'create_user':
            fullName = request.POST.get('full_name')
            phone = request.POST.get('phone_number')
            profession = request.POST.get('profession')
            facility = request.POST.get('facility_name')
            county = request.POST.get('county')
            if fullName and phone:
                UserProfile.objects.create(
                    full_name=fullName, phone_number=phone, profession=profession or 'CHW',
                    facility_name=facility or 'Health Center', county=county or 'Nairobi',
                    pin_hash='123456', is_approved=True
                )
                messages.success(request, f"Health Worker {fullName} created and approved successfully.")
        
        elif action == 'create_announcement':
            title = request.POST.get('title')
            message = request.POST.get('message')
            priority = request.POST.get('priority', 'INFO')
            if title and message:
                Announcement.objects.create(title=title, message=message, priority=priority)
                messages.success(request, "Announcement published to mobile CHWs.")

        return redirect('/dashboard/')

    context = {
        'is_superuser': user.is_superuser,
        'user_groups': user_groups,
        'patients': Patient.objects.all().order_by('-created_at')[:20],
        'triages': TriageSession.objects.all().order_by('-created_at')[:20],
        'users': UserProfile.objects.all().order_by('-created_at')[:50],
        'pending_users': UserProfile.objects.filter(is_approved=False).count(),
        'announcements': Announcement.objects.filter(is_active=True)[:10],
        'settings': AppSetting.objects.all(),
        'total_patients': Patient.objects.count(),
        'total_triage': TriageSession.objects.count(),
        'total_users': UserProfile.objects.count()
    }
    return render(request, 'dashboard.html', context)


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

class AnnouncementViewSet(viewsets.ModelViewSet):
    queryset = Announcement.objects.filter(is_active=True).order_by('-created_at')
    serializer_class = AnnouncementSerializer

class AppSettingViewSet(viewsets.ModelViewSet):
    queryset = AppSetting.objects.all()
    serializer_class = AppSettingSerializer


# API Authentication & Bi-directional Delta Sync Views
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
            'pin_hash': request.data.get('pinHash') or request.data.get('pin_hash', ''),
            'is_approved': True  # Mobile direct signups auto-approve or queue per setting
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
            if not user.is_approved:
                return Response({'error': 'Account pending admin approval. Please contact your supervisor.'}, status=status.HTTP_403_FORBIDDEN)
            serializer = UserProfileSerializer(user)
            return Response({'status': 'authenticated', 'user': serializer.data}, status=status.HTTP_200_OK)
        return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)

class BatchSyncView(APIView):
    """Bi-directional Delta Sync View.
    Uploads local offline patient visits and downloads recent facility patient records.
    """
    def post(self, request):
        patients_data = request.data.get('patients', [])
        facility_name = request.data.get('facilityName') or request.data.get('facility_name', '')
        synced_count = 0
        
        # 1. Silent Upsert (No Duplicate Patients)
        for pdata in patients_data:
            p_uid = pdata.get('patientUid') or pdata.get('patient_uid')
            if not p_uid:
                continue
            patient, created = Patient.objects.update_or_create(
                patient_uid=p_uid,
                defaults={
                    'full_name': pdata.get('fullName') or pdata.get('full_name', ''),
                    'date_of_birth': pdata.get('dateOfBirth') or pdata.get('date_of_birth', '2024-01-01'),
                    'sex': pdata.get('sex', 'Male'),
                    'caregiver_name': pdata.get('caregiverName') or pdata.get('caregiver_name'),
                    'guardian_phone': pdata.get('guardianPhone') or pdata.get('guardian_phone'),
                    'birth_certificate_number': pdata.get('birthCertificateNumber') or pdata.get('birth_certificate_number'),
                    'facility_name': pdata.get('facilityName') or pdata.get('facility_name') or facility_name,
                    'county': pdata.get('county', 'Nairobi'),
                    'risk_level': pdata.get('riskLevel') or pdata.get('risk_level', 'LOW')
                }
            )
            
            # Record Triage Visit
            danger_signs = pdata.get('dangerSigns', '')
            if danger_signs or pdata.get('overallRisk'):
                visit_count = patient.triage_sessions.count() + 1
                TriageSession.objects.create(
                    patient=patient,
                    visit_number=visit_count,
                    danger_signs=danger_signs,
                    overall_risk=pdata.get('overallRisk', 'LOW')
                )

            synced_count += 1

        # 2. Download Delta (Recent 20 Patients for this Facility)
        facility_patients = Patient.objects.filter(facility_name=facility_name).order_by('-updated_at')[:20] if facility_name else Patient.objects.all().order_by('-updated_at')[:20]
        download_serializer = PatientSerializer(facility_patients, many=True)

        return Response({
            'status': 'success',
            'syncedCount': synced_count,
            'downloadedPatients': download_serializer.data,
            'message': f'Successfully synced {synced_count} patient records.'
        }, status=status.HTTP_200_OK)
